package org.springframework.samples.petclinic.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.samples.petclinic.model.EmergencyConsultation;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.TriagePriority;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.repository.EmergencyConsultationRepository;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.repository.PetRepository;
import org.springframework.samples.petclinic.repository.VetRepository;
import org.springframework.samples.petclinic.rest.dto.EmergencyConsultationConfirmationDto;
import org.springframework.samples.petclinic.rest.dto.EmergencyConsultationRequestDto;
import org.springframework.samples.petclinic.rest.exception.ResourceNotFoundException;
import org.springframework.samples.petclinic.rest.exception.SlotAlreadyBookedException;
import org.springframework.samples.petclinic.rest.exception.UnauthorizedPetAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class EmergencyConsultationService {

    private static final Logger logger = LoggerFactory.getLogger(EmergencyConsultationService.class);

    private final EmergencyConsultationRepository emergencyConsultationRepository;
    private final PetRepository petRepository;
    private final VetRepository vetRepository;
    private final OwnerRepository ownerRepository;

    public EmergencyConsultationService(
            EmergencyConsultationRepository emergencyConsultationRepository,
            PetRepository petRepository,
            VetRepository vetRepository,
            OwnerRepository ownerRepository) {
        this.emergencyConsultationRepository = emergencyConsultationRepository;
        this.petRepository = petRepository;
        this.vetRepository = vetRepository;
        this.ownerRepository = ownerRepository;
    }

    @Transactional
    public EmergencyConsultationConfirmationDto bookEmergencyConsultation(
            EmergencyConsultationRequestDto request,
            String authenticatedUsername) {

        Pet pet = findPetById(request.petId());
        Vet vet = findVetById(request.vetId());

        validatePetOwnership(pet, authenticatedUsername);
        validateVetAvailability(request.vetId(), request.requestedTime());

        TriagePriority priority = calculatePriority(request.severity());
        Instant confirmedAt = Instant.now();

        EmergencyConsultation consultation = createConsultation(
            pet, vet, request, priority, confirmedAt
        );

        EmergencyConsultation savedConsultation = saveConsultation(consultation);

        logger.info("Emergency consultation booked: bookingId={}, petId={}, vetId={}, priority={}",
            savedConsultation.getId(), pet.getId(), vet.getId(), priority);

        return buildConfirmation(savedConsultation, pet, vet);
    }

    private Pet findPetById(Long petId) {
        return petRepository.findById(petId)
            .orElseThrow(() -> new ResourceNotFoundException("Pet not found with id: " + petId));
    }

    private Vet findVetById(Long vetId) {
        return vetRepository.findById(vetId)
            .orElseThrow(() -> new ResourceNotFoundException("Vet not found with id: " + vetId));
    }

    private void validatePetOwnership(Pet pet, String authenticatedUsername) {
        if (authenticatedUsername == null) {
            throw new UnauthorizedPetAccessException("Authentication required to book consultations");
        }

        Owner owner = ownerRepository.findByLastName(authenticatedUsername).stream()
            .filter(o -> o.getPets().contains(pet))
            .findFirst()
            .orElse(null);

        if (owner == null) {
            throw new UnauthorizedPetAccessException(
                "You are not authorized to book consultations for this pet"
            );
        }
    }

    private void validateVetAvailability(Long vetId, Instant requestedTime) {
        if (emergencyConsultationRepository.existsByVetIdAndScheduledTime(vetId, requestedTime)) {
            throw new SlotAlreadyBookedException(
                "The requested vet time slot is already booked"
            );
        }
    }

    private TriagePriority calculatePriority(org.springframework.samples.petclinic.model.SymptomSeverity severity) {
        return TriagePriority.fromSeverity(severity);
    }

    private EmergencyConsultation createConsultation(
            Pet pet,
            Vet vet,
            EmergencyConsultationRequestDto request,
            TriagePriority priority,
            Instant confirmedAt) {

        EmergencyConsultation consultation = new EmergencyConsultation();
        consultation.setPet(pet);
        consultation.setVet(vet);
        consultation.setScheduledTime(request.requestedTime());
        consultation.setSeverity(request.severity());
        consultation.setPriority(priority);
        consultation.setSymptoms(request.symptoms());
        consultation.setConfirmedAt(confirmedAt);
        return consultation;
    }

    private EmergencyConsultation saveConsultation(EmergencyConsultation consultation) {
        try {
            return emergencyConsultationRepository.save(consultation);
        } catch (DataIntegrityViolationException e) {
            throw new SlotAlreadyBookedException(
                "The requested vet time slot is already booked (concurrent booking detected)"
            );
        }
    }

    private EmergencyConsultationConfirmationDto buildConfirmation(
            EmergencyConsultation consultation,
            Pet pet,
            Vet vet) {

        return new EmergencyConsultationConfirmationDto(
            consultation.getId(),
            pet.getId(),
            pet.getName(),
            vet.getId(),
            vet.getFirstName() + " " + vet.getLastName(),
            consultation.getScheduledTime(),
            consultation.getSeverity(),
            consultation.getPriority(),
            consultation.getConfirmedAt()
        );
    }
}
