package org.springframework.samples.petclinic.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OwnerLastNameContainingSearchServiceTest {

    @Mock
    private OwnerRepository ownerRepository;

    @InjectMocks
    private ClinicServiceImpl clinicService;

    static Stream<String> nullOrBlankInputs() {
        return Stream.of(null, "", " ", "   ", "\t", "\n", "\r\n");
    }

    @Nested
    class NonPaged {

        @ParameterizedTest
        @MethodSource("org.springframework.samples.petclinic.service.OwnerLastNameContainingSearchServiceTest#nullOrBlankInputs")
        @DisplayName("findOwnerByLastNameContaining: null/blank input -> empty list and repository not invoked")
        void findOwnerByLastNameContaining_nullOrBlank_returnsEmptyListAndDoesNotCallRepo(String input) {
            Collection<Owner> result = clinicService.findOwnerByLastNameContaining(input);

            assertThat(result).isNotNull().isEmpty();
            verifyNoInteractions(ownerRepository);
        }

        static Stream<String> tooLongInputs() {
            return Stream.of("a".repeat(101), "   " + "b".repeat(101) + "   ");
        }

        @ParameterizedTest
        @MethodSource("tooLongInputs")
        @DisplayName("findOwnerByLastNameContaining: >MAX length after trim -> empty list and repository not invoked")
        void findOwnerByLastNameContaining_tooLong_returnsEmptyListAndDoesNotCallRepo(String input) {
            Collection<Owner> result = clinicService.findOwnerByLastNameContaining(input);

            assertThat(result).isNotNull().isEmpty();
            verifyNoInteractions(ownerRepository);
        }

        static Stream<org.junit.jupiter.params.provider.Arguments> validInputsAndExpectedTrimmed() {
            return Stream.of(
                org.junit.jupiter.params.provider.Arguments.of("Smith", "Smith"),
                org.junit.jupiter.params.provider.Arguments.of(" smith ", "smith"),
                org.junit.jupiter.params.provider.Arguments.of("SMI", "SMI"),
                org.junit.jupiter.params.provider.Arguments.of("o'c", "o'c"),
                org.junit.jupiter.params.provider.Arguments.of("Mc", "Mc")
            );
        }

        @ParameterizedTest
        @MethodSource("validInputsAndExpectedTrimmed")
        @DisplayName("findOwnerByLastNameContaining: trims input and delegates to repository")
        void findOwnerByLastNameContaining_valid_trimsAndDelegates(String input, String expectedTrimmed) {
            List<Owner> expected = List.of(new Owner(), new Owner());
            when(ownerRepository.findByLastNameContainingIgnoreCase(expectedTrimmed)).thenReturn(expected);

            Collection<Owner> result = clinicService.findOwnerByLastNameContaining(input);

            assertThat(result).isSameAs(expected);
            verify(ownerRepository, times(1)).findByLastNameContainingIgnoreCase(expectedTrimmed);
            verifyNoMoreInteractions(ownerRepository);
        }

        @Test
        @DisplayName("findOwnerByLastNameContaining: repository returns empty list -> service returns empty list (not null)")
        void findOwnerByLastNameContaining_noMatches_returnsEmptyListNotNull() {
            when(ownerRepository.findByLastNameContainingIgnoreCase("nomatch")).thenReturn(Collections.emptyList());

            Collection<Owner> result = clinicService.findOwnerByLastNameContaining("nomatch");

            assertThat(result).isNotNull().isEmpty();
            verify(ownerRepository).findByLastNameContainingIgnoreCase("nomatch");
            verifyNoMoreInteractions(ownerRepository);
        }
    }

    @Nested
    class Paged {

        @ParameterizedTest
        @MethodSource("org.springframework.samples.petclinic.service.OwnerLastNameContainingSearchServiceTest#nullOrBlankInputs")
        @DisplayName("findOwnerByLastNameContaining(pageable): null/blank input -> empty page and repository not invoked")
        void findOwnerByLastNameContainingPaged_nullOrBlank_returnsEmptyPageAndDoesNotCallRepo(String input) {
            Pageable pageable = PageRequest.of(0, 5);

            Page<Owner> result = clinicService.findOwnerByLastNameContaining(input, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
            verifyNoInteractions(ownerRepository);
        }

        static Stream<String> tooLongInputs() {
            return Stream.of("a".repeat(101), "   " + "b".repeat(101) + "   ");
        }

        @ParameterizedTest
        @MethodSource("tooLongInputs")
        @DisplayName("findOwnerByLastNameContaining(pageable): >MAX length after trim -> empty page and repository not invoked")
        void findOwnerByLastNameContainingPaged_tooLong_returnsEmptyPageAndDoesNotCallRepo(String input) {
            Pageable pageable = PageRequest.of(1, 10);

            Page<Owner> result = clinicService.findOwnerByLastNameContaining(input, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
            verifyNoInteractions(ownerRepository);
        }

        static Stream<org.junit.jupiter.params.provider.Arguments> validInputsAndExpectedTrimmed() {
            return Stream.of(
                org.junit.jupiter.params.provider.Arguments.of("Smith", "Smith"),
                org.junit.jupiter.params.provider.Arguments.of(" smith ", "smith"),
                org.junit.jupiter.params.provider.Arguments.of("SMI", "SMI")
            );
        }

        @ParameterizedTest
        @MethodSource("validInputsAndExpectedTrimmed")
        @DisplayName("findOwnerByLastNameContaining(pageable): trims input and delegates to repository")
        void findOwnerByLastNameContainingPaged_valid_trimsAndDelegates(String input, String expectedTrimmed) {
            Pageable pageable = PageRequest.of(0, 2);
            Page<Owner> expectedPage = Page.empty(pageable);
            when(ownerRepository.findByLastNameContainingIgnoreCase(expectedTrimmed, pageable)).thenReturn(expectedPage);

            Page<Owner> result = clinicService.findOwnerByLastNameContaining(input, pageable);

            assertThat(result).isSameAs(expectedPage);
            verify(ownerRepository, times(1)).findByLastNameContainingIgnoreCase(expectedTrimmed, pageable);
            verifyNoMoreInteractions(ownerRepository);
        }
    }
}
