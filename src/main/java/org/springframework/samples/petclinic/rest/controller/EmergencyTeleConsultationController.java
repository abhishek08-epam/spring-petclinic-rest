/*
 * Copyright 2016-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import org.springframework.samples.petclinic.rest.dto.EmergencyTeleConsultationConfirmationDto;
import org.springframework.samples.petclinic.rest.dto.EmergencyTeleConsultationRequestDto;
import org.springframework.samples.petclinic.service.TeleConsultationBookingService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public REST controller for emergency tele-consultation bookings.
 * Implements AC-1: public endpoint with no authentication required.
 *
 * @author PetClinic Team
 */
@RestController
@RequestMapping("/api/public/emergency-teleconsultations")
@Tag(name = "Emergency Tele-Consultations", description = "Public endpoint for emergency pet tele-consultation bookings")
public class EmergencyTeleConsultationController {

	private final TeleConsultationBookingService bookingService;

	public EmergencyTeleConsultationController(TeleConsultationBookingService bookingService) {
		this.bookingService = bookingService;
	}

	/**
	 * Book an emergency tele-consultation.
	 * Implements AC-1, AC-2, AC-3, AC-4.
	 *
	 * @param request the booking request
	 * @return confirmation DTO with HTTP 201 Created
	 */
	@PostMapping
	@Operation(
		summary = "Book emergency tele-consultation",
		description = "Creates an emergency video consultation booking for a pet with automatic triage priority calculation and vet assignment. " +
			"No authentication required for speed. Best-effort vet availability check is performed with timeout."
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "201",
			description = "Emergency consultation booked successfully",
			content = @Content(schema = @Schema(implementation = EmergencyTeleConsultationConfirmationDto.class))
		),
		@ApiResponse(
			responseCode = "400",
			description = "Invalid request data (validation failed)"
		),
		@ApiResponse(
			responseCode = "404",
			description = "Pet not found or no vets available in the system"
		),
		@ApiResponse(
			responseCode = "500",
			description = "Internal server error during booking"
		)
	})
	public ResponseEntity<EmergencyTeleConsultationConfirmationDto> createEmergencyBooking(
		@Valid @RequestBody EmergencyTeleConsultationRequestDto request
	) {
		EmergencyTeleConsultationConfirmationDto confirmation = bookingService.createEmergencyBooking(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(confirmation);
	}

}
