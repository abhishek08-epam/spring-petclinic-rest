package org.springframework.samples.petclinic.teleconsultation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = false)
@Schema(description = "Emergency tele-consultation booking request")
public record EmergencyTeleConsultationRequest(

    @Schema(description = "Pet identifier", example = "1", required = true)
    @NotNull(message = "Pet ID is required")
    @Positive(message = "Pet ID must be positive")
    Long petId,

    @Schema(description = "Symptom severity", example = "SEVERE", required = true, allowableValues = {"MILD", "MODERATE", "SEVERE"})
    @NotNull(message = "Severity is required")
    TeleConsultationBooking.Severity severity,

    @Schema(description = "Symptoms or reason for consultation", example = "Difficulty breathing", required = true)
    @NotBlank(message = "Symptoms are required")
    @Size(min = 10, max = 1000, message = "Symptoms must be between 10 and 1000 characters")
    String symptoms,

    @Schema(description = "Requested start time", example = "2024-01-15T14:30:00Z")
    Instant requestedStart,

    @Schema(description = "Communication channel", example = "VIDEO", defaultValue = "VIDEO")
    TeleConsultationBooking.Channel channel

) {
    public EmergencyTeleConsultationRequest {
        if (channel == null) {
            channel = TeleConsultationBooking.Channel.VIDEO;
        }
    }
}