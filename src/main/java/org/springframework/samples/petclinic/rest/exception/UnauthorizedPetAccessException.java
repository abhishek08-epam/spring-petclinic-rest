package org.springframework.samples.petclinic.rest.exception;

public class UnauthorizedPetAccessException extends RuntimeException {
    public UnauthorizedPetAccessException(String message) {
        super(message);
    }
}
