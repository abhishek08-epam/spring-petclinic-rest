package org.springframework.samples.petclinic.teleconsultation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teleconsultations")
@Tag(name = "Emergency Tele-Consultation", description = "Emergency video consultation booking API")
public class EmergencyTeleConsultationController {

    private final TeleConsultationBookingService bookingService;

    public EmergencyTeleConsultationController(TeleConsultationBookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/bookings")
    @Operation(
        summary = "Book emergency tele-consultation",
        description = "Submit an emergency video consultation request for a pet. Automatically calculates triage priority and attempts to assign an available vet."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Booking created successfully",
            content = @Content(schema = @Schema(implementation = EmergencyTeleConsultationConfirmation.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Pet not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Vet slot conflict",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "429",
            description = "Too many requests - rate limit exceeded",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<EmergencyTeleConsultationConfirmation> createEmergencyBooking(
        @Valid @RequestBody EmergencyTeleConsultationRequest request
    ) {
        EmergencyTeleConsultationConfirmation confirmation = bookingService.createEmergencyBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(confirmation);
    }
}