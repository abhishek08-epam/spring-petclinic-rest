package org.springframework.samples.petclinic.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.model.SymptomSeverity;
import org.springframework.samples.petclinic.model.TriagePriority;
import org.springframework.samples.petclinic.rest.dto.EmergencyConsultationConfirmationDto;
import org.springframework.samples.petclinic.rest.dto.EmergencyConsultationRequestDto;
import org.springframework.samples.petclinic.rest.exception.ResourceNotFoundException;
import org.springframework.samples.petclinic.rest.exception.SlotAlreadyBookedException;
import org.springframework.samples.petclinic.rest.exception.UnauthorizedPetAccessException;
import org.springframework.samples.petclinic.service.EmergencyConsultationService;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.samples.petclinic.rest.assertions.EmergencyConsultationJsonAssertions.assertConfirmationHasCoreFields;
import static org.springframework.samples.petclinic.rest.assertions.EmergencyConsultationJsonAssertions.assertPriority;
import static org.springframework.samples.petclinic.rest.assertions.EmergencyConsultationJsonAssertions.assertSeverity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EmergencyConsultationController.class)
class EmergencyConsultationControllerMockMvcTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean EmergencyConsultationService emergencyConsultationService;
    @MockBean Authentication authentication;

    static Stream<SymptomSeverityCase> severityCases() {
        return Stream.of(
            new SymptomSeverityCase(SymptomSeverity.MILD, TriagePriority.LOW),
            new SymptomSeverityCase(SymptomSeverity.MODERATE, TriagePriority.MEDIUM),
            new SymptomSeverityCase(SymptomSeverity.SEVERE, TriagePriority.HIGH)
        );
    }

    record SymptomSeverityCase(SymptomSeverity severity, TriagePriority expectedPriority) {}

    @ParameterizedTest
    @MethodSource("severityCases")
    @DisplayName("POST /api/emergency-consultations returns 201 with assigned priority (mapped from severity)")
    void bookEmergencyConsultation_success_returns201AndConfirmation_withPriority(SymptomSeverityCase tc) throws Exception {
        Instant scheduled = Instant.now().plusSeconds(3600);

        EmergencyConsultationRequestDto request = new EmergencyConsultationRequestDto(
            1L, 2L, scheduled, tc.severity(), "pet is unwell"
        );

        EmergencyConsultationConfirmationDto confirmation = new EmergencyConsultationConfirmationDto(
            10L, 1L, "Buddy", 2L, "Sam Vet", scheduled, tc.severity(), tc.expectedPriority(), Instant.now()
        );

        when(authentication.getName()).thenReturn("owner1");
        when(emergencyConsultationService.bookEmergencyConsultation(any(EmergencyConsultationRequestDto.class), eq("owner1")))
            .thenReturn(confirmation);

        var ra = mockMvc.perform(post("/api/emergency-consultations")
            .principal(authentication)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)));

        assertConfirmationHasCoreFields(ra.andExpect(status().isCreated()));
        assertSeverity(ra, tc.severity().name());
        assertPriority(ra, tc.expectedPriority().name());
    }

    @Test
    @DisplayName("POST /api/emergency-consultations returns 400 when request body fails validation (missing required fields)")
    void bookEmergencyConsultation_invalidBody_returns400() throws Exception {
        String invalidJson = "{}";

        mockMvc.perform(post("/api/emergency-consultations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/emergency-consultations returns 400 when requestedTime is in the past (compact constructor validation)")
    void bookEmergencyConsultation_pastRequestedTime_returns400() throws Exception {
        Instant past = Instant.now().minusSeconds(60);

        String json = """
            {
              "petId": 1,
              "vetId": 2,
              "requestedTime": "%s",
              "severity": "MILD",
              "symptoms": "test"
            }
            """.formatted(past.toString());

        mockMvc.perform(post("/api/emergency-consultations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/emergency-consultations returns 404 when service throws ResourceNotFoundException")
    void bookEmergencyConsultation_petOrVetNotFound_returns404() throws Exception {
        Instant scheduled = Instant.now().plusSeconds(3600);
        EmergencyConsultationRequestDto request = new EmergencyConsultationRequestDto(
            1L, 2L, scheduled, SymptomSeverity.SEVERE, "urgent"
        );

        when(authentication.getName()).thenReturn("owner1");
        when(emergencyConsultationService.bookEmergencyConsultation(any(EmergencyConsultationRequestDto.class), eq("owner1")))
            .thenThrow(new ResourceNotFoundException("Pet not found with id: 1"));

        mockMvc.perform(post("/api/emergency-consultations")
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/emergency-consultations returns 409 when slot already booked")
    void bookEmergencyConsultation_slotAlreadyBooked_returns409() throws Exception {
        Instant scheduled = Instant.now().plusSeconds(3600);
        EmergencyConsultationRequestDto request = new EmergencyConsultationRequestDto(
            1L, 2L, scheduled, SymptomSeverity.MODERATE, "urgent"
        );

        when(authentication.getName()).thenReturn("owner1");
        when(emergencyConsultationService.bookEmergencyConsultation(any(EmergencyConsultationRequestDto.class), eq("owner1")))
            .thenThrow(new SlotAlreadyBookedException("The requested vet time slot is already booked"));

        mockMvc.perform(post("/api/emergency-consultations")
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/emergency-consultations returns 403 when attempting to book for a pet not owned by authenticated user")
    void bookEmergencyConsultation_unauthorizedPetAccess_returns403() throws Exception {
        Instant scheduled = Instant.now().plusSeconds(3600);
        EmergencyConsultationRequestDto request = new EmergencyConsultationRequestDto(
            1L, 2L, scheduled, SymptomSeverity.MILD, "help"
        );

        when(authentication.getName()).thenReturn("owner1");
        when(emergencyConsultationService.bookEmergencyConsultation(any(EmergencyConsultationRequestDto.class), eq("owner1")))
            .thenThrow(new UnauthorizedPetAccessException("You are not authorized to book consultations for this pet"));

        mockMvc.perform(post("/api/emergency-consultations")
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }

    @Nested
    class ContentNegotiation {

        @Test
        @DisplayName("POST /api/emergency-consultations returns 415 when content-type is not application/json")
        void bookEmergencyConsultation_wrongContentType_returns415() throws Exception {
            mockMvc.perform(post("/api/emergency-consultations")
                    .contentType(MediaType.TEXT_PLAIN)
                    .content("not json"))
                .andExpect(status().isUnsupportedMediaType());
        }
    }
}
