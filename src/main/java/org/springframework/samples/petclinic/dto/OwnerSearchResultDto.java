package org.springframework.samples.petclinic.dto;

import jakarta.validation.constraints.NotNull;

public record OwnerSearchResultDto(
    @NotNull Integer id,
    @NotNull String firstName,
    @NotNull String lastName,
    @NotNull String address,
    @NotNull String city,
    @NotNull String telephone
) {
}
