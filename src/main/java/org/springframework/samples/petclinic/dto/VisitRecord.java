package org.springframework.samples.petclinic.dto;

import java.time.LocalDate;
import java.util.Objects;

public record VisitRecord(LocalDate date, String description, Integer id, Integer petId) {

    public VisitRecord {
        date = Objects.requireNonNull(date, "date must not be null");
        description = Objects.requireNonNull(description, "description must not be null");
        if (description.isBlank()) {
            throw new IllegalArgumentException("description must not be blank");
        }
        if (description.length() > 255) {
            throw new IllegalArgumentException("description must not exceed 255 characters");
        }
        id = Objects.requireNonNull(id, "id must not be null");
        petId = Objects.requireNonNull(petId, "petId must not be null");
        if (id < 0) {
            throw new IllegalArgumentException("id must not be negative");
        }
        if (petId < 0) {
            throw new IllegalArgumentException("petId must not be negative");
        }
    }
}
