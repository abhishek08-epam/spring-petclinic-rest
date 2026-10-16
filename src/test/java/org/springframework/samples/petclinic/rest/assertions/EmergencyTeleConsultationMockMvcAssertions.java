package org.springframework.samples.petclinic.rest.assertions;

import org.springframework.samples.petclinic.rest.dto.EmergencyTeleConsultationConfirmationDto;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public final class EmergencyTeleConsultationMockMvcAssertions {

	private EmergencyTeleConsultationMockMvcAssertions() {
	}

	public static ResultMatcher assertEmergencyBookingConfirmation(EmergencyTeleConsultationConfirmationDto expected) {
		return result -> {
			jsonPath("$.bookingId").value(expected.bookingId()).match(result);
			jsonPath("$.petId").value(expected.petId()).match(result);
			jsonPath("$.vetId").value(expected.vetId()).match(result);

			jsonPath("$.startAt").value(expected.startAt().toString()).match(result);
			jsonPath("$.endAt").value(expected.endAt().toString()).match(result);

			jsonPath("$.triagePriority").value(expected.triagePriority().name()).match(result);
			jsonPath("$.status").value(expected.status()).match(result);

			if (expected.meetingUrl() != null) {
				jsonPath("$.meetingUrl").value(expected.meetingUrl()).match(result);
			}
		};
	}
}
