package org.springframework.samples.petclinic.teleconsultation;

public class PetNotFoundException extends RuntimeException {

    private final Long petId;

    public PetNotFoundException(Long petId) {
        super("Pet not found with ID: " + petId);
        this.petId = petId;
    }

    public Long getPetId() {
        return petId;
    }
}