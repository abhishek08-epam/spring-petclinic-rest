package org.springframework.samples.petclinic.teleconsultation;

public class VetSlotConflictException extends RuntimeException {

    public VetSlotConflictException(String message) {
        super(message);
    }
}