package org.springframework.samples.petclinic.teleconsultation;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Error response")
public record ErrorResponse(

    @Schema(description = "HTTP status code", example = "400")
    int status,

    @Schema(description = "Error message", example = "Invalid request")
    String message,

    @Schema(description = "Detailed error information", example = "Pet ID is required")
    String detail,

    @Schema(description = "Timestamp of error", example = "2024-01-15T14:30:00Z")
    Instant timestamp,

    @Schema(description = "Request path", example = "/api/teleconsultations/bookings")
    String path

) {
    public ErrorResponse(int status, String message, String detail, String path) {
        this(status, message, detail, Instant.now(), path);
    }
}