package org.springframework.samples.petclinic.rest.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class VetDtoEmailSanitizationTest {

    static Stream<SanitizeCase> sanitizeCases() {
        return Stream.of(
            new SanitizeCase(null, null),
            new SanitizeCase("", null),
            new SanitizeCase("   ", null),
            new SanitizeCase(" vet@example.com ", "vet@example.com"),
            new SanitizeCase("\tvet@example.com\t", "vet@example.com")
        );
    }

    @ParameterizedTest(name = "sanitize \"{0}\" -> \"{1}\"")
    @MethodSource("sanitizeCases")
    @DisplayName("VetDto email: trim + blank-to-null sanitization is null-safe")
    void setEmail_shouldSanitizeNullSafe(SanitizeCase c) {
        VetDto dto = new VetDto();
        dto.setEmail(c.input);
        assertThat(dto.getEmail()).isEqualTo(c.expected);
    }

    static class SanitizeCase {
        final String input;
        final String expected;

        SanitizeCase(String input, String expected) {
            this.input = input;
            this.expected = expected;
        }

        @Override
        public String toString() {
            return input;
        }
    }
}
