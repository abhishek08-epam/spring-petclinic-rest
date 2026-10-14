package org.springframework.samples.petclinic.rest.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.samples.petclinic.rest.exception.ResourceNotFoundException;
import org.springframework.samples.petclinic.rest.exception.SlotAlreadyBookedException;
import org.springframework.samples.petclinic.rest.exception.UnauthorizedPetAccessException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class EmergencyConsultationExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(ResourceNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            ex.getMessage()
        );
        problemDetail.setTitle("Resource Not Found");
        return problemDetail;
    }

    @ExceptionHandler(SlotAlreadyBookedException.class)
    public ProblemDetail handleSlotAlreadyBooked(SlotAlreadyBookedException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            ex.getMessage()
        );
        problemDetail.setTitle("Booking Conflict");
        return problemDetail;
    }

    @ExceptionHandler(UnauthorizedPetAccessException.class)
    public ProblemDetail handleUnauthorizedPetAccess(UnauthorizedPetAccessException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.FORBIDDEN,
            ex.getMessage()
        );
        problemDetail.setTitle("Forbidden");
        return problemDetail;
    }
}
