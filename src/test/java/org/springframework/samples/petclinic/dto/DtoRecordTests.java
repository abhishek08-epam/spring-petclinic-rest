/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.samples.petclinic.rest.dto.PetTypeDto;
import org.springframework.samples.petclinic.rest.dto.VisitDto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DtoRecordTests {

    @Test
    void petRecordShouldCreateValidRecord() {
        LocalDate birthDate = LocalDate.now().minusYears(2);
        PetTypeDto type = petType();
        VisitDto visit = visitDto();

        PetRecord petRecord = new PetRecord("Leo", birthDate, type, 1, 2, List.of(visit));

        assertThat(petRecord.name()).isEqualTo("Leo");
        assertThat(petRecord.birthDate()).isEqualTo(birthDate);
        assertThat(petRecord.type()).isSameAs(type);
        assertThat(petRecord.id()).isEqualTo(1);
        assertThat(petRecord.ownerId()).isEqualTo(2);
        assertThat(petRecord.visits()).containsExactly(visit);
    }

    @Test
    void petRecordShouldNormalizeNullVisitsToImmutableEmptyList() {
        PetRecord petRecord = new PetRecord("Leo", LocalDate.now().minusYears(2), petType(), 1, null, null);

        assertThat(petRecord.ownerId()).isNull();
        assertThat(petRecord.visits()).isEmpty();
        assertThatThrownBy(() -> petRecord.visits().add(visitDto()))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void petRecordShouldCopyVisitsDefensively() {
        List<VisitDto> visits = new ArrayList<>();
        visits.add(visitDto());

        PetRecord petRecord = new PetRecord("Leo", LocalDate.now().minusYears(2), petType(), 1, 2, visits);
        visits.clear();

        assertThat(petRecord.visits()).hasSize(1);
        assertThatThrownBy(() -> petRecord.visits().clear())
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void petRecordShouldUseRecordValueSemantics() {
        LocalDate birthDate = LocalDate.now().minusYears(2);
        PetTypeDto type = petType();
        VisitDto visit = visitDto();
        PetRecord petRecord = new PetRecord("Leo", birthDate, type, 1, 2, List.of(visit));
        PetRecord samePetRecord = new PetRecord("Leo", birthDate, type, 1, 2, List.of(visit));

        assertThat(petRecord).isEqualTo(samePetRecord);
        assertThat(petRecord).hasSameHashCodeAs(samePetRecord);
        assertThat(petRecord.toString()).contains("Leo", "ownerId=2");
    }

    @ParameterizedTest
    @MethodSource("invalidPetRecords")
    void petRecordShouldRejectInvalidValues(PetRecordFactory factory, Class<? extends Throwable> exceptionType,
                                            String message) {
        assertThatThrownBy(factory::create)
            .isInstanceOf(exceptionType)
            .hasMessage(message);
    }

    @Test
    void visitRecordShouldCreateValidRecord() {
        LocalDate date = LocalDate.now();

        VisitRecord visitRecord = new VisitRecord(date, "rabies shot", 1, 2);

        assertThat(visitRecord.date()).isEqualTo(date);
        assertThat(visitRecord.description()).isEqualTo("rabies shot");
        assertThat(visitRecord.id()).isEqualTo(1);
        assertThat(visitRecord.petId()).isEqualTo(2);
    }

    @Test
    void visitRecordShouldUseRecordValueSemantics() {
        LocalDate date = LocalDate.now();
        VisitRecord visitRecord = new VisitRecord(date, "rabies shot", 1, 2);
        VisitRecord sameVisitRecord = new VisitRecord(date, "rabies shot", 1, 2);

        assertThat(visitRecord).isEqualTo(sameVisitRecord);
        assertThat(visitRecord).hasSameHashCodeAs(sameVisitRecord);
        assertThat(visitRecord.toString()).contains("rabies shot", "petId=2");
    }

    @ParameterizedTest
    @MethodSource("invalidVisitRecords")
    void visitRecordShouldRejectInvalidValues(VisitRecordFactory factory, Class<? extends Throwable> exceptionType,
                                              String message) {
        assertThatThrownBy(factory::create)
            .isInstanceOf(exceptionType)
            .hasMessage(message);
    }

    private static Stream<Arguments> invalidPetRecords() {
        PetTypeDto type = petType();
        LocalDate birthDate = LocalDate.now().minusYears(2);
        return Stream.of(
            Arguments.of(
                (PetRecordFactory) () -> new PetRecord(null, birthDate, type, 1, null, List.of()),
                NullPointerException.class,
                "name must not be null"
            ),
            Arguments.of(
                (PetRecordFactory) () -> new PetRecord(" ", birthDate, type, 1, null, List.of()),
                IllegalArgumentException.class,
                "name must not be blank"
            ),
            Arguments.of(
                (PetRecordFactory) () -> new PetRecord("1234567890123456789012345678901", birthDate, type, 1, null,
                    List.of()),
                IllegalArgumentException.class,
                "name must not exceed 30 characters"
            ),
            Arguments.of(
                (PetRecordFactory) () -> new PetRecord("Leo", null, type, 1, null, List.of()),
                NullPointerException.class,
                "birthDate must not be null"
            ),
            Arguments.of(
                (PetRecordFactory) () -> new PetRecord("Leo", LocalDate.now().plusDays(1), type, 1, null, List.of()),
                IllegalArgumentException.class,
                "birthDate must not be in the future"
            ),
            Arguments.of(
                (PetRecordFactory) () -> new PetRecord("Leo", birthDate, null, 1, null, List.of()),
                NullPointerException.class,
                "type must not be null"
            ),
            Arguments.of(
                (PetRecordFactory) () -> new PetRecord("Leo", birthDate, type, null, null, List.of()),
                NullPointerException.class,
                "id must not be null"
            ),
            Arguments.of(
                (PetRecordFactory) () -> new PetRecord("Leo", birthDate, type, -1, null, List.of()),
                IllegalArgumentException.class,
                "id must not be negative"
            ),
            Arguments.of(
                (PetRecordFactory) () -> new PetRecord("Leo", birthDate, type, 1, -1, List.of()),
                IllegalArgumentException.class,
                "ownerId must not be negative"
            )
        );
    }

    private static Stream<Arguments> invalidVisitRecords() {
        LocalDate date = LocalDate.now();
        return Stream.of(
            Arguments.of(
                (VisitRecordFactory) () -> new VisitRecord(null, "rabies shot", 1, 2),
                NullPointerException.class,
                "date must not be null"
            ),
            Arguments.of(
                (VisitRecordFactory) () -> new VisitRecord(date, null, 1, 2),
                NullPointerException.class,
                "description must not be null"
            ),
            Arguments.of(
                (VisitRecordFactory) () -> new VisitRecord(date, " ", 1, 2),
                IllegalArgumentException.class,
                "description must not be blank"
            ),
            Arguments.of(
                (VisitRecordFactory) () -> new VisitRecord(date, "a".repeat(256), 1, 2),
                IllegalArgumentException.class,
                "description must not exceed 255 characters"
            ),
            Arguments.of(
                (VisitRecordFactory) () -> new VisitRecord(date, "rabies shot", null, 2),
                NullPointerException.class,
                "id must not be null"
            ),
            Arguments.of(
                (VisitRecordFactory) () -> new VisitRecord(date, "rabies shot", 1, null),
                NullPointerException.class,
                "petId must not be null"
            ),
            Arguments.of(
                (VisitRecordFactory) () -> new VisitRecord(date, "rabies shot", -1, 2),
                IllegalArgumentException.class,
                "id must not be negative"
            ),
            Arguments.of(
                (VisitRecordFactory) () -> new VisitRecord(date, "rabies shot", 1, -1),
                IllegalArgumentException.class,
                "petId must not be negative"
            )
        );
    }

    private static PetTypeDto petType() {
        return new PetTypeDto().id(1).name("cat");
    }

    private static VisitDto visitDto() {
        return new VisitDto().id(1).petId(2).date(LocalDate.now()).description("rabies shot");
    }

    @FunctionalInterface
    private interface PetRecordFactory {

        PetRecord create();
    }

    @FunctionalInterface
    private interface VisitRecordFactory {

        VisitRecord create();
    }
}
