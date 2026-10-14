package org.springframework.samples.petclinic.rest.assertions;

import org.hamcrest.Matchers;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Instant;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public final class EmergencyConsultationJsonAssertions {

    private EmergencyConsultationJsonAssertions() {
    }

    public static ResultActions assertConfirmationHasCoreFields(ResultActions ra) throws Exception {
        return ra.andExpect(jsonPath("$.bookingId").isNumber())
            .andExpect(jsonPath("$.petId").isNumber())
            .andExpect(jsonPath("$.petName").isString())
            .andExpect(jsonPath("$.vetId").isNumber())
            .andExpect(jsonPath("$.vetName").isString())
            .andExpect(jsonPath("$.scheduledTime").isString())
            .andExpect(jsonPath("$.severity").isString())
            .andExpect(jsonPath("$.priority").isString())
            .andExpect(jsonPath("$.confirmedAt").isString());
    }

    public static ResultActions assertPriority(ResultActions ra, String expectedPriority) throws Exception {
        return ra.andExpect(jsonPath("$.priority").value(expectedPriority));
    }

    public static ResultActions assertSeverity(ResultActions ra, String expectedSeverity) throws Exception {
        return ra.andExpect(jsonPath("$.severity").value(expectedSeverity));
    }

    public static ResultActions assertScheduledTime(ResultActions ra, Instant expected) throws Exception {
        return ra.andExpect(jsonPath("$.scheduledTime").value(expected.toString()));
    }

    public static ResultActions assertBookingIdPositive(ResultActions ra) throws Exception {
        return ra.andExpect(jsonPath("$.bookingId").value(Matchers.greaterThan(0)));
    }
}
