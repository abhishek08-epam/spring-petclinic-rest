package org.springframework.samples.petclinic.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.samples.petclinic.model.SymptomSeverity;

import java.time.Instant;

public record EmergencyConsultationRequestDto(
    @NotNull(message = "Pet ID is required")
    @Positive(message = "Pet ID must be positive")
    Long petId,
    
    @NotNull(message = "Vet ID is required")
    @Positive(message = "Vet ID must be positive")
    Long vetId,
    
    @NotNull(message = "Requested time is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    Instant requestedTime,
    
    @NotNull(message = "Symptom severity is required")
    SymptomSeverity severity,
    
    @NotBlank(message = "Symptoms description is required")
    @Size(max = 2000, message = "Symptoms description must not exceed 2000 characters")
    String symptoms
) {
    public EmergencyConsultationRequestDto {
        if (petId != null && petId <= 0) {
            throw new IllegalArgumentException("Pet ID must be positive");
        }
        if (vetId != null && vetId <= 0) {
            throw new IllegalArgumentException("Vet ID must be positive");
        }
        if (requestedTime != null && requestedTime.isBefore(Instant.now())) {
            throw new IllegalArgumentException("Requested time must be in the future");
        }
        if (symptoms != null && symptoms.length() > 2000) {
            throw new IllegalArgumentException("Symptoms description must not exceed 2000 characters");
        }
    }
}
