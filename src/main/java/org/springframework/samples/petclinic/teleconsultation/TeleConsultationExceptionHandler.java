package org.springframework.samples.petclinic.teleconsultation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.stream.Collectors;

@RestControllerAdvice(assignableTypes = EmergencyTeleConsultationController.class)
public class TeleConsultationExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(TeleConsultationExceptionHandler.class);

    @ExceptionHandler(PetNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePetNotFoundException(
        PetNotFoundException ex,
        WebRequest request
    ) {
        logger.warn("Pet not found: petId={}", ex.getPetId());

        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            "Pet not found",
            ex.getMessage(),
            extractPath(request)
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(VetSlotConflictException.class)
    public ResponseEntity<ErrorResponse> handleVetSlotConflictException(
        VetSlotConflictException ex,
        WebRequest request
    ) {
        logger.warn("Vet slot conflict: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
            HttpStatus.CONFLICT.value(),
            "Booking conflict",
            ex.getMessage(),
            extractPath(request)
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
        MethodArgumentNotValidException ex,
        WebRequest request
    ) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
            .map(this::formatFieldError)
            .collect(Collectors.joining("; "));

        logger.warn("Validation failed: {}", detail);

        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation failed",
            detail,
            extractPath(request)
        );

        return ResponseEntity.badRequest().body(error);
    }

    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }

    private String extractPath(WebRequest request) {
        String description = request.getDescription(false);
        return description.replace("uri=", "");
    }
}