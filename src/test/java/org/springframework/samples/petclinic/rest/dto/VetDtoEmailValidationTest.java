package org.springframework.samples.petclinic.rest.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.samples.petclinic.rest.assertions.BeanValidationAssertions;

import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class VetDtoEmailValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDownValidator() {
        if (validatorFactory != null) {
            validatorFactory.close();
        }
    }

    static Stream<String> validEmailsOrNullInputs() {
        return Stream.of(
            null,
            "",
            "   ",
            "vet@example.com",
            "VET@example.com",
            "vet.name+tag@example.co.uk",
            " vet@example.com ",
            "a@b.co"
        );
    }

    @ParameterizedTest(name = "email=\"{0}\" should be accepted (null/blank or valid format)")
    @MethodSource("validEmailsOrNullInputs")
    @DisplayName("VetDto email: null/blank accepted; valid emails accepted")
    void email_nullBlankOrValid_shouldPassValidation(String input) {
        VetDto dto = new VetDto();
        dto.setFirstName("James");
        dto.setLastName("Carter");
        dto.setEmail(input);

        Set<ConstraintViolation<VetDto>> violations = validator.validate(dto);
        BeanValidationAssertions.assertNoViolationOnProperty(violations, "email");

        if (input == null || input.trim().isEmpty()) {
            assertThat(dto.getEmail()).isNull();
        } else {
            assertThat(dto.getEmail()).isEqualTo(input.trim());
        }
    }

    static Stream<String> invalidEmails() {
        return Stream.of(
            "not-an-email",
            "a@",
            "@b.com",
            "a@b",
            "a b@c.com",
            "a@b..com",
            "a@b.com\nx",
            "a@b.com\rx",
            "a@b.com\t"
        );
    }

    @ParameterizedTest(name = "email=\"{0}\" should be rejected")
    @MethodSource("invalidEmails")
    @DisplayName("VetDto email: invalid formats rejected and control characters rejected")
    void email_invalid_shouldFailValidation(String input) {
        VetDto dto = new VetDto();
        dto.setFirstName("James");
        dto.setLastName("Carter");
        dto.setEmail(input);

        Set<ConstraintViolation<VetDto>> violations = validator.validate(dto);
        BeanValidationAssertions.assertHasViolationOnProperty(violations, "email");
    }
}
