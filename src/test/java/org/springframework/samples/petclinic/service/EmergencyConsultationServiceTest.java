package org.springframework.samples.petclinic.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.samples.petclinic.model.*;
import org.springframework.samples.petclinic.repository.EmergencyConsultationRepository;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.repository.PetRepository;
import org.springframework.samples.petclinic.repository.VetRepository;
import org.springframework.samples.petclinic.rest.dto.EmergencyConsultationConfirmationDto;
import org.springframework.samples.petclinic.rest.dto.EmergencyConsultationRequestDto;
import org.springframework.samples.petclinic.rest.exception.ResourceNotFoundException;
import org.springframework.samples.petclinic.rest.exception.SlotAlreadyBookedException;
import org.springframework.samples.petclinic.rest.exception.UnauthorizedPetAccessException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EmergencyConsultationServiceTest {

    EmergencyConsultationRepository emergencyConsultationRepository = mock(EmergencyConsultationRepository.class);
    PetRepository petRepository = mock(PetRepository.class);
    VetRepository vetRepository = mock(VetRepository.class);
    OwnerRepository ownerRepository = mock(OwnerRepository.class);

    EmergencyConsultationService service = new EmergencyConsultationService(
        emergencyConsultationRepository, petRepository, vetRepository, ownerRepository
    );

    @ParameterizedTest
    @EnumSource(SymptomSeverity.class)
    @DisplayName("Service maps severity to expected triage priority in confirmation DTO")
    void bookEmergencyConsultation_mapsSeverityToPriority(SymptomSeverity severity) {
        Instant scheduled = Instant.now().plusSeconds(3600);

        Owner owner = new Owner();
        owner.setFirstName("A");
        owner.setLastName("owner1");

        Pet pet = new Pet();
        pet.setId(1);
        pet.setName("Buddy");
        pet.setOwner(owner);
        owner.addPet(pet);

        Vet vet = new Vet();
        vet.setId(2);
        vet.setFirstName("Sam");
        vet.setLastName("Vet");

        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        when(vetRepository.findById(2L)).thenReturn(Optional.of(vet));
        when(ownerRepository.findByLastName("owner1")).thenReturn(List.of(owner));
        when(emergencyConsultationRepository.existsByVetIdAndScheduledTime(2L, scheduled)).thenReturn(false);

        when(emergencyConsultationRepository.save(any(EmergencyConsultation.class)))
            .thenAnswer(inv -> {
                EmergencyConsultation ec = inv.getArgument(0);
                ec.setId(10);
                return ec;
            });

        EmergencyConsultationConfirmationDto dto = service.bookEmergencyConsultation(
            new EmergencyConsultationRequestDto(1L, 2L, scheduled, severity, "symptoms"),
            "owner1"
        );

        TriagePriority expected = TriagePriority.fromSeverity(severity);
        assertEquals(expected, dto.priority());
        assertEquals(severity, dto.severity());
        assertEquals(1L, dto.petId());
        assertEquals(2L, dto.vetId());
        assertEquals(scheduled, dto.scheduledTime());
        assertTrue(dto.bookingId() > 0);
        assertNotNull(dto.confirmedAt());
    }

    @Test
    @DisplayName("Service throws 404-like exception when pet not found")
    void bookEmergencyConsultation_petNotFound_throwsResourceNotFound() {
        Instant scheduled = Instant.now().plusSeconds(3600);
        when(petRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.bookEmergencyConsultation(
            new EmergencyConsultationRequestDto(1L, 2L, scheduled, SymptomSeverity.MILD, "symptoms"),
            "owner1"
        ));
    }

    @Test
    @DisplayName("Service throws 404-like exception when vet not found")
    void bookEmergencyConsultation_vetNotFound_throwsResourceNotFound() {
        Instant scheduled = Instant.now().plusSeconds(3600);

        Owner owner = new Owner();
        owner.setLastName("owner1");
        Pet pet = new Pet();
        pet.setId(1);
        pet.setOwner(owner);
        owner.addPet(pet);

        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        when(vetRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.bookEmergencyConsultation(
            new EmergencyConsultationRequestDto(1L, 2L, scheduled, SymptomSeverity.MILD, "symptoms"),
            "owner1"
        ));
    }

    @Test
    @DisplayName("Service forbids booking when authentication username is null")
    void bookEmergencyConsultation_noAuth_throwsUnauthorizedPetAccess() {
        Instant scheduled = Instant.now().plusSeconds(3600);

        Owner owner = new Owner();
        owner.setLastName("owner1");
        Pet pet = new Pet();
        pet.setId(1);
        pet.setOwner(owner);
        owner.addPet(pet);

        Vet vet = new Vet();
        vet.setId(2);

        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        when(vetRepository.findById(2L)).thenReturn(Optional.of(vet));

        assertThrows(UnauthorizedPetAccessException.class, () -> service.bookEmergencyConsultation(
            new EmergencyConsultationRequestDto(1L, 2L, scheduled, SymptomSeverity.MILD, "symptoms"),
            null
        ));
    }

    @Test
    @DisplayName("Service forbids booking for pet not owned by authenticated user")
    void bookEmergencyConsultation_wrongOwner_throwsUnauthorizedPetAccess() {
        Instant scheduled = Instant.now().plusSeconds(3600);

        Owner actualOwner = new Owner();
        actualOwner.setLastName("someoneElse");

        Pet pet = new Pet();
        pet.setId(1);
        pet.setOwner(actualOwner);
        actualOwner.addPet(pet);

        Vet vet = new Vet();
        vet.setId(2);

        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        when(vetRepository.findById(2L)).thenReturn(Optional.of(vet));
        when(ownerRepository.findByLastName("owner1")).thenReturn(List.of(new Owner()));

        assertThrows(UnauthorizedPetAccessException.class, () -> service.bookEmergencyConsultation(
            new EmergencyConsultationRequestDto(1L, 2L, scheduled, SymptomSeverity.MILD, "symptoms"),
            "owner1"
        ));
    }

    @Test
    @DisplayName("Service prevents overbooking when slot already booked (pre-check)")
    void bookEmergencyConsultation_slotExists_throwsSlotAlreadyBooked() {
        Instant scheduled = Instant.now().plusSeconds(3600);

        Owner owner = new Owner();
        owner.setLastName("owner1");
        Pet pet = new Pet();
        pet.setId(1);
        pet.setOwner(owner);
        owner.addPet(pet);

        Vet vet = new Vet();
        vet.setId(2);

        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        when(vetRepository.findById(2L)).thenReturn(Optional.of(vet));
        when(ownerRepository.findByLastName("owner1")).thenReturn(List.of(owner));
        when(emergencyConsultationRepository.existsByVetIdAndScheduledTime(2L, scheduled)).thenReturn(true);

        assertThrows(SlotAlreadyBookedException.class, () -> service.bookEmergencyConsultation(
            new EmergencyConsultationRequestDto(1L, 2L, scheduled, SymptomSeverity.SEVERE, "symptoms"),
            "owner1"
        ));

        verify(emergencyConsultationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Service maps DataIntegrityViolationException to SlotAlreadyBookedException (concurrent booking)")
    void bookEmergencyConsultation_uniqueConstraintViolation_throwsSlotAlreadyBooked() {
        Instant scheduled = Instant.now().plusSeconds(3600);

        Owner owner = new Owner();
        owner.setLastName("owner1");
        Pet pet = new Pet();
        pet.setId(1);
        pet.setOwner(owner);
        owner.addPet(pet);

        Vet vet = new Vet();
        vet.setId(2);

        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        when(vetRepository.findById(2L)).thenReturn(Optional.of(vet));
        when(ownerRepository.findByLastName("owner1")).thenReturn(List.of(owner));
        when(emergencyConsultationRepository.existsByVetIdAndScheduledTime(2L, scheduled)).thenReturn(false);
        when(emergencyConsultationRepository.save(any(EmergencyConsultation.class)))
            .thenThrow(new DataIntegrityViolationException("unique"));

        assertThrows(SlotAlreadyBookedException.class, () -> service.bookEmergencyConsultation(
            new EmergencyConsultationRequestDto(1L, 2L, scheduled, SymptomSeverity.MODERATE, "symptoms"),
            "owner1"
        ));
    }
}
