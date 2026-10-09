package org.springframework.samples.petclinic.rest.assertions;

import jakarta.validation.ConstraintViolation;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public final class BeanValidationAssertions {

    private BeanValidationAssertions() {
    }

    public static <T> void assertHasViolationOnProperty(Set<ConstraintViolation<T>> violations, String property) {
        assertThat(violations)
            .as("Expected at least one constraint violation")
            .isNotEmpty();

        assertThat(violations.stream().anyMatch(v -> propertyPathEquals(v, property)))
            .as("Expected a violation on property '%s' but got: %s", property, violationsToString(violations))
            .isTrue();
    }

    public static <T> void assertNoViolationOnProperty(Set<ConstraintViolation<T>> violations, String property) {
        assertThat(violations.stream().noneMatch(v -> propertyPathEquals(v, property)))
            .as("Expected no violation on property '%s' but got: %s", property, violationsToString(violations))
            .isTrue();
    }

    private static boolean propertyPathEquals(ConstraintViolation<?> v, String property) {
        return v.getPropertyPath() != null && property.equals(v.getPropertyPath().toString());
    }

    private static String violationsToString(Set<? extends ConstraintViolation<?>> violations) {
        return violations.stream()
            .map(v -> v.getPropertyPath() + ": " + v.getMessage() + " (invalid=" + v.getInvalidValue() + ")")
            .sorted()
            .reduce((a, b) -> a + "; " + b)
            .orElse("");
    }
}
