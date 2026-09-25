package org.springframework.samples.petclinic.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.springframework.samples.petclinic.rest.dto.PetTypeDto;
import org.springframework.samples.petclinic.rest.dto.VisitDto;

public record PetDto(String name, LocalDate birthDate, PetTypeDto type, Integer id, Integer ownerId,
                     List<VisitDto> visits) {

    public PetDto {
        name = Objects.requireNonNull(name, "name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (name.length() > 30) {
            throw new IllegalArgumentException("name must not exceed 30 characters");
        }
        birthDate = Objects.requireNonNull(birthDate, "birthDate must not be null");
        if (birthDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("birthDate must not be in the future");
        }
        type = Objects.requireNonNull(type, "type must not be null");
        id = Objects.requireNonNull(id, "id must not be null");
        if (id < 0) {
            throw new IllegalArgumentException("id must not be negative");
        }
        if (ownerId != null && ownerId < 0) {
            throw new IllegalArgumentException("ownerId must not be negative");
        }
        visits = List.copyOf(visits == null ? List.of() : visits);
    }
}
