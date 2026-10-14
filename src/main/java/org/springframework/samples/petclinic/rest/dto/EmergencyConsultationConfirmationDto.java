package org.springframework.samples.petclinic.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.samples.petclinic.model.SymptomSeverity;
import org.springframework.samples.petclinic.model.TriagePriority;

import java.time.Instant;

public record EmergencyConsultationConfirmationDto(
    Long bookingId,
    Long petId,
    String petName,
    Long vetId,
    String vetName,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    Instant scheduledTime,
    SymptomSeverity severity,
    TriagePriority priority,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    Instant confirmedAt
) {
    public EmergencyConsultationConfirmationDto {
        if (bookingId == null || bookingId <= 0) {
            throw new IllegalArgumentException("Booking ID must be positive");
        }
        if (petId == null || petId <= 0) {
            throw new IllegalArgumentException("Pet ID must be positive");
        }
        if (vetId == null || vetId <= 0) {
            throw new IllegalArgumentException("Vet ID must be positive");
        }
        if (scheduledTime == null) {
            throw new IllegalArgumentException("Scheduled time is required");
        }
        if (severity == null) {
            throw new IllegalArgumentException("Severity is required");
        }
        if (priority == null) {
            throw new IllegalArgumentException("Priority is required");
        }
        if (confirmedAt == null) {
            throw new IllegalArgumentException("Confirmation timestamp is required");
        }
    }
}
