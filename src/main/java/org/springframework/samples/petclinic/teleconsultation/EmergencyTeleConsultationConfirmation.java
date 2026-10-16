package org.springframework.samples.petclinic.teleconsultation;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Emergency tele-consultation booking confirmation")
public record EmergencyTeleConsultationConfirmation(

    @Schema(description = "Booking reference identifier", example = "TC-2024-001234")
    String bookingReference,

    @Schema(description = "Current booking status", example = "TRIAGED")
    TeleConsultationBooking.BookingStatus status,

    @Schema(description = "Calculated triage priority", example = "RED")
    TeleConsultationBooking.TriageLevel triagePriority,

    @Schema(description = "Scheduled start time if assigned", example = "2024-01-15T14:30:00Z")
    Instant scheduledStart,

    @Schema(description = "Assigned vet identifier if available", example = "3")
    Long vetId,

    @Schema(description = "Pet identifier", example = "1")
    Long petId

) {
}