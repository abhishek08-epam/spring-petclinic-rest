package org.springframework.samples.petclinic.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.rest.dto.EmergencyConsultationConfirmationDto;
import org.springframework.samples.petclinic.rest.dto.EmergencyConsultationRequestDto;
import org.springframework.samples.petclinic.service.EmergencyConsultationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/emergency-consultations")
@Tag(name = "emergency-consultations", description = "Emergency tele-consultation booking API")
public class EmergencyConsultationController {

    private final EmergencyConsultationService emergencyConsultationService;

    public EmergencyConsultationController(EmergencyConsultationService emergencyConsultationService) {
        this.emergencyConsultationService = emergencyConsultationService;
    }

    @PostMapping
    @Operation(
        summary = "Book an emergency tele-consultation",
        description = "Allows a pet owner to book an emergency video consultation for their pet with automatic triage priority assignment based on symptom severity"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Emergency consultation successfully booked",
            content = @Content(schema = @Schema(implementation = EmergencyConsultationConfirmationDto.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data (e.g., invalid severity, missing required fields)"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - attempting to book for a pet not owned by the authenticated user"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Pet or vet not found"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Conflict - requested vet/time slot is already booked"
        )
    })
    public ResponseEntity<EmergencyConsultationConfirmationDto> bookEmergencyConsultation(
            @Valid @RequestBody EmergencyConsultationRequestDto request,
            Authentication authentication) {
        
        String authenticatedUsername = authentication != null ? authentication.getName() : null;
        EmergencyConsultationConfirmationDto confirmation = 
            emergencyConsultationService.bookEmergencyConsultation(request, authenticatedUsername);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(confirmation);
    }
}
