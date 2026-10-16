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
package org.springframework.samples.petclinic.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.samples.petclinic.model.TeleConsultationBooking.Severity;

import java.time.Instant;

/**
 * DTO for emergency tele-consultation booking requests.
 * Validation is applied in the compact constructor following Java 21 record patterns.
 *
 * @author PetClinic Team
 */
@Schema(description = "Request to book an emergency tele-consultation for a pet")
public record EmergencyTeleConsultationRequestDto(

	@JsonProperty("petId")
	@NotNull(message = "Pet ID is required")
	@Positive(message = "Pet ID must be positive")
	@Schema(description = "ID of the pet requiring consultation", example = "1")
	Integer petId,

	@JsonProperty("severity")
	@Schema(description = "Symptom severity level (defaults to MILD if not provided)", example = "MODERATE")
	Severity severity,

	@JsonProperty("reason")
	@NotNull(message = "Reason for consultation is required")
	@Schema(description = "Description of symptoms or reason for emergency consultation", example = "Pet is vomiting and lethargic")
	String reason,

	@JsonProperty("preferredStartAt")
	@Schema(description = "Preferred start time for consultation (defaults to now if not provided)", example = "2024-01-15T10:30:00Z")
	Instant preferredStartAt,

	@JsonProperty("durationMinutes")
	@Schema(description = "Duration of consultation in minutes (defaults to 15)", example = "15")
	Integer durationMinutes
) {

	private static final int DEFAULT_DURATION_MINUTES = 15;

	/**
	 * Compact constructor with validation and default value assignment.
	 */
	public EmergencyTeleConsultationRequestDto {
		// Validate and apply defaults
		if (reason != null && reason.isBlank()) {
			throw new IllegalArgumentException("Reason cannot be blank");
		}
		if (durationMinutes == null || durationMinutes <= 0) {
			durationMinutes = DEFAULT_DURATION_MINUTES;
		}
	}

	/**
	 * Get severity with default fallback to MILD per boundary case BC-1.
	 */
	public Severity getSeverityOrDefault() {
		return severity != null ? severity : Severity.MILD;
	}

	/**
	 * Get preferred start time with default fallback to current instant.
	 */
	public Instant getPreferredStartAtOrNow() {
		return preferredStartAt != null ? preferredStartAt : Instant.now();
	}

}
