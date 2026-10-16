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
import org.springframework.samples.petclinic.model.TeleConsultationBooking.TriagePriority;

import java.time.Instant;

/**
 * Confirmation DTO returned after successful emergency tele-consultation booking.
 * Contains minimal booking details without exposing sensitive internal data.
 *
 * @author PetClinic Team
 */
@Schema(description = "Confirmation of an emergency tele-consultation booking")
public record EmergencyTeleConsultationConfirmationDto(

	@JsonProperty("bookingId")
	@Schema(description = "Unique booking identifier", example = "42")
	Integer bookingId,

	@JsonProperty("petId")
	@Schema(description = "ID of the pet for this consultation", example = "1")
	Integer petId,

	@JsonProperty("vetId")
	@Schema(description = "ID of the assigned veterinarian", example = "3")
	Integer vetId,

	@JsonProperty("startAt")
	@Schema(description = "Scheduled start time of the consultation", example = "2024-01-15T10:30:00Z")
	Instant startAt,

	@JsonProperty("endAt")
	@Schema(description = "Scheduled end time of the consultation", example = "2024-01-15T10:45:00Z")
	Instant endAt,

	@JsonProperty("triagePriority")
	@Schema(description = "Calculated triage priority based on severity", example = "MEDIUM")
	TriagePriority triagePriority,

	@JsonProperty("status")
	@Schema(description = "Current booking status", example = "CONFIRMED")
	String status,

	@JsonProperty("meetingUrl")
	@Schema(description = "Video meeting URL for the consultation", example = "https://petclinic.meet/room/1-1705318200000")
	String meetingUrl
) {
}
