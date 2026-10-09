package org.springframework.samples.petclinic.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "A veterinarian representation")
public class VetDto {

    @Schema(description = "The ID of the vet")
    private Integer id;

    @Schema(description = "The first name of the vet")
    private String firstName;

    @Schema(description = "The last name of the vet")
    private String lastName;

    @Schema(description = "The email address of the vet (optional)")
    @Email(message = "must be a valid email address")
    @Size(max = 254, message = "must not exceed 254 characters")
    @Pattern(regexp = "^[^\\r\\n\\t\\0]*$", message = "must not contain control characters")
    private String email;

    @Schema(description = "The specialties of the vet")
    private List<SpecialtyDto> specialties;

    public VetDto() {
        this.specialties = new ArrayList<>();
    }

    public VetDto(Integer id, String firstName, String lastName, String email, List<SpecialtyDto> specialties) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = sanitizeEmail(email);
        this.specialties = specialties != null ? specialties : new ArrayList<>();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = sanitizeEmail(email);
    }

    public List<SpecialtyDto> getSpecialties() {
        return specialties;
    }

    public void setSpecialties(List<SpecialtyDto> specialties) {
        this.specialties = specialties;
    }

    @JsonIgnore
    public int getNrOfSpecialties() {
        return specialties.size();
    }

    public void addSpecialty(SpecialtyDto specialty) {
        this.specialties.add(specialty);
    }

    private String sanitizeEmail(String rawEmail) {
        if (rawEmail == null) {
            return null;
        }
        String trimmed = rawEmail.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
