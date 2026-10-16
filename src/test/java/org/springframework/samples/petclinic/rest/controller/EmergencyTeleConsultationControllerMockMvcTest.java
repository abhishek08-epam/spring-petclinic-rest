package org.springframework.samples.petclinic.rest.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.model.TeleConsultationBooking;
import org.springframework.samples.petclinic.rest.config.PublicEndpointSecurityConfig;
import org.springframework.samples.petclinic.rest.dto.EmergencyTeleConsultationConfirmationDto;
import org.springframework.samples.petclinic.service.TeleConsultationBookingService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.samples.petclinic.rest.assertions.EmergencyTeleConsultationMockMvcAssertions.assertEmergencyBookingConfirmation;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EmergencyTeleConsultationController.class)
@Import(PublicEndpointSecurityConfig.class)
class EmergencyTeleConsultationControllerMockMvcTest {

	@Autowired
	MockMvc mockMvc;

	@MockitoBean
	TeleConsultationBookingService bookingService;

	@Test
	@DisplayName("AC-1/BC-3: endpoint is public (no auth) and returns 201 with confirmation")
	void createEmergencyBooking_publicEndpoint_shouldReturnCreated() throws Exception {
		Instant startAt = Instant.parse("2026-01-01T10:00:00Z");
		Instant endAt = Instant.parse("2026-01-01T10:15:00Z");

		EmergencyTeleConsultationConfirmationDto confirmation = new EmergencyTeleConsultationConfirmationDto(
			42,
			1,
			3,
			startAt,
			endAt,
			TeleConsultationBooking.TriagePriority.HIGH,
			"CONFIRMED",
			"https://petclinic.meet/room/1-123"
		);

		when(bookingService.createEmergencyBooking(any())).thenReturn(confirmation);

		mockMvc.perform(post("/api/public/emergency-teleconsultations")
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON)
					.content("""
						{
						  "petId": 1,
						  "severity": "SEVERE",
						  "reason": "Pet is struggling to breathe",
						  "preferredStartAt": "2026-01-01T10:00:00Z",
						  "durationMinutes": 15
						}
						"""))
			.andExpect(status().isCreated())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andExpect(assertEmergencyBookingConfirmation(confirmation));

		verify(bookingService, times(1)).createEmergencyBooking(any());
	}

	@ParameterizedTest
	@ValueSource(strings = {
		// BC-1: missing severity -> should still be accepted; service will default it
		"""
		{
		  "petId": 1,
		  "reason": "Coughing",
		  "preferredStartAt": "2026-01-01T10:00:00Z",
		  "durationMinutes": 15
		}
		""",
		// minimal body: startAt and duration omitted (defaults happen in DTO)
		"""
		{
		  "petId": 1,
		  "severity": "MILD",
		  "reason": "Coughing"
		}
		"""
	})
	@DisplayName("BC-1: missing optional fields (severity/startAt/duration) should not be rejected by controller")
	void createEmergencyBooking_missingOptionalFields_shouldNotReturnBadRequest(String body) throws Exception {
		Instant startAt = Instant.parse("2026-01-01T10:00:00Z");
		Instant endAt = Instant.parse("2026-01-01T10:15:00Z");

		when(bookingService.createEmergencyBooking(any())).thenReturn(
			new EmergencyTeleConsultationConfirmationDto(
				1,
				1,
				3,
				startAt,
				endAt,
				TeleConsultationBooking.TriagePriority.LOW,
				"CONFIRMED",
				"https://petclinic.meet/room/1-abc"
			)
		);

		mockMvc.perform(post("/api/public/emergency-teleconsultations")
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON)
					.content(body))
			.andExpect(status().isCreated());

		verify(bookingService, times(1)).createEmergencyBooking(any());
	}

	@Test
	@DisplayName("Validation failure: missing petId should return 400")
	void createEmergencyBooking_missingPetId_shouldReturnBadRequest() throws Exception {
		mockMvc.perform(post("/api/public/emergency-teleconsultations")
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON)
					.content("""
						{
						  "severity": "MILD",
						  "reason": "Coughing",
						  "preferredStartAt": "2026-01-01T10:00:00Z",
						  "durationMinutes": 15
						}
						"""))
			.andExpect(status().isBadRequest());

		verify(bookingService, never()).createEmergencyBooking(any());
	}

	@Test
	@DisplayName("Validation failure: negative petId should return 400")
	void createEmergencyBooking_negativePetId_shouldReturnBadRequest() throws Exception {
		mockMvc.perform(post("/api/public/emergency-teleconsultations")
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON)
					.content("""
						{
						  "petId": -1,
						  "severity": "MILD",
						  "reason": "Coughing",
						  "preferredStartAt": "2026-01-01T10:00:00Z",
						  "durationMinutes": 15
						}
						"""))
			.andExpect(status().isBadRequest());

		verify(bookingService, never()).createEmergencyBooking(any());
	}

	@Test
	@DisplayName("Validation failure: missing reason should return 400")
	void createEmergencyBooking_missingReason_shouldReturnBadRequest() throws Exception {
		mockMvc.perform(post("/api/public/emergency-teleconsultations")
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON)
					.content("""
						{
						  "petId": 1,
						  "severity": "MILD",
						  "preferredStartAt": "2026-01-01T10:00:00Z",
						  "durationMinutes": 15
						}
						"""))
			.andExpect(status().isBadRequest());

		verify(bookingService, never()).createEmergencyBooking(any());
	}

	@Test
	@DisplayName("Validation failure: blank reason should return 400 (record constructor throws IllegalArgumentException)")
	void createEmergencyBooking_blankReason_shouldReturnBadRequest() throws Exception {
		mockMvc.perform(post("/api/public/emergency-teleconsultations")
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON)
					.content("""
						{
						  "petId": 1,
						  "severity": "MILD",
						  "reason": "   ",
						  "preferredStartAt": "2026-01-01T10:00:00Z",
						  "durationMinutes": 15
						}
						"""))
			.andExpect(status().isBadRequest());

		verify(bookingService, never()).createEmergencyBooking(any());
	}
}
