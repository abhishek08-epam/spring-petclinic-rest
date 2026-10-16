package org.springframework.samples.petclinic.teleconsultation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EmergencyTeleConsultationController.class)
@Import(TeleConsultationExceptionHandler.class)
class EmergencyTeleConsultationControllerUnknownFieldTest {

    @Autowired MockMvc mockMvc;

    @MockBean TeleConsultationBookingService bookingService;

    private static final String ENDPOINT = "/api/teleconsultations/bookings";

    @Test
    @DisplayName("Boundary: unknown JSON fields are rejected (ignoreUnknown=false) -> 400")
    void createBooking_unknownField_returns400() throws Exception {
        mockMvc.perform(
                post(ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"petId":1,"severity":"MILD","symptoms":"Mild limp noticed since morning","unknownField":"boom"}
                        """)
            )
            .andExpect(status().isBadRequest());
    }
}
