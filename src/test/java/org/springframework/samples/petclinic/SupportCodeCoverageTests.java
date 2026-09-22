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

package org.springframework.samples.petclinic;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.model.Role;
import org.springframework.samples.petclinic.model.Specialty;
import org.springframework.samples.petclinic.model.User;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.rest.advice.ExceptionControllerAdvice;
import org.springframework.samples.petclinic.rest.controller.BindingErrorsResponse;
import org.springframework.samples.petclinic.rest.controller.v1.RootRestControllerV1;
import org.springframework.samples.petclinic.security.Roles;
import org.springframework.samples.petclinic.util.EntityUtils;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SupportCodeCoverageTests {

    @Test
    void rootControllerShouldRedirectToSwaggerUi() throws Exception {
        RootRestControllerV1 controller = new RootRestControllerV1();
        ReflectionTestUtils.setField(controller, "servletContextPath", "/petclinic");
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.redirectToSwagger(response);

        assertThat(response.getRedirectedUrl()).isEqualTo("/petclinic/swagger-ui/index.html");
    }

    @Test
    void bindingErrorsResponseShouldSerializeBodyIdAndFieldErrors() {
        BindingErrorsResponse bodyOnlyIdError = new BindingErrorsResponse(null, 5);
        BindingErrorsResponse mismatchedIdError = new BindingErrorsResponse(1, 2);
        BindingErrorsResponse fieldErrors = new BindingErrorsResponse();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new TestForm(), "testForm");
        bindingResult.rejectValue("name", "required", "must not be blank");

        fieldErrors.addAllErrors(bindingResult);

        assertThat(bodyOnlyIdError.toJSON()).contains("must not be specified", "\"fieldName\":\"id\"");
        assertThat(mismatchedIdError.toJSON()).contains("does not match pathId: 1", "\"fieldValue\":\"2\"");
        assertThat(fieldErrors.toJSON()).contains("testForm", "name", "must not be blank");
        assertThat(fieldErrors.toString()).contains("BindingErrorsResponse");
    }

    @Test
    void entityUtilsShouldFindByIdAndThrowWhenMissing() {
        PetType cat = petType(1, "cat");
        PetType dog = petType(2, "dog");

        PetType found = EntityUtils.getById(List.of(cat, dog), PetType.class, 2);

        assertThat(found).isSameAs(dog);
        assertThatExceptionOfType(ObjectRetrievalFailureException.class)
            .isThrownBy(() -> EntityUtils.getById(List.of(cat, dog), PetType.class, 99));
    }

    @Test
    void userAndRoleShouldExposeAccessorsAndAddRoles() {
        User user = new User();
        user.setUsername("admin");
        user.setPassword("secret");
        user.setEnabled(true);
        user.addRole("ROLE_ADMIN");

        Role role = user.getRoles().iterator().next();
        role.setUser(user);

        assertThat(user.getUsername()).isEqualTo("admin");
        assertThat(user.getPassword()).isEqualTo("secret");
        assertThat(user.getEnabled()).isTrue();
        assertThat(role.getName()).isEqualTo("ROLE_ADMIN");
        assertThat(role.getUser()).isSameAs(user);
    }

    @Test
    void vetSpecialtiesShouldBeSortedAndUnmodifiable() {
        Vet vet = new Vet();
        vet.setFirstName("James");
        vet.setLastName("Carter");
        Specialty surgery = specialty(1, "surgery");
        Specialty dentistry = specialty(2, "dentistry");

        vet.addSpecialty(surgery);
        vet.addSpecialty(dentistry);

        assertThat(vet.getFirstName()).isEqualTo("James");
        assertThat(vet.getLastName()).isEqualTo("Carter");
        assertThat(vet.getNrOfSpecialties()).isEqualTo(2);
        assertThat(vet.getSpecialties()).extracting(Specialty::getName).containsExactly("dentistry", "surgery");
        assertThatThrownBy(() -> vet.getSpecialties().add(specialty(3, "radiology")))
            .isInstanceOf(UnsupportedOperationException.class);

        vet.clearSpecialties();

        assertThat(vet.getNrOfSpecialties()).isZero();
    }

    @Test
    void ownerAndPetHelpersShouldManageRelationshipsAndSorting() {
        Owner owner = new Owner();
        owner.setId(1);
        owner.setFirstName("George");
        owner.setLastName("Franklin");
        owner.setAddress("110 W. Liberty St.");
        owner.setCity("Madison");
        owner.setTelephone("6085551023");
        Pet rosy = pet(1, "Rosy", LocalDate.now().minusYears(2));
        Pet jewel = pet(2, "Jewel", LocalDate.now().minusYears(3));

        owner.addPet(rosy);
        owner.addPet(jewel);

        assertThat(owner.getAddress()).isEqualTo("110 W. Liberty St.");
        assertThat(owner.getCity()).isEqualTo("Madison");
        assertThat(owner.getTelephone()).isEqualTo("6085551023");
        assertThat(owner.getPet("Rosy")).isSameAs(rosy);
        assertThat(owner.getPet("missing")).isNull();
        assertThat(owner.getPet(2)).isSameAs(jewel);
        assertThat(owner.getPets()).extracting(Pet::getName).containsExactly("Jewel", "Rosy");
        assertThat(owner.toString()).contains("George", "Franklin", "Madison");
        assertThat(rosy.getOwner()).isSameAs(owner);
    }

    @Test
    void petVisitsShouldBeSortedAndUnmodifiable() {
        Pet pet = pet(1, "Leo", LocalDate.now().minusYears(4));
        Visit olderVisit = visit(1, LocalDate.now().minusDays(2), "checkup");
        Visit newerVisit = visit(2, LocalDate.now().minusDays(1), "rabies shot");

        pet.addVisit(olderVisit);
        pet.addVisit(newerVisit);

        assertThat(pet.getBirthDate()).isBefore(LocalDate.now());
        assertThat(pet.getType().getName()).isEqualTo("dog");
        assertThat(pet.getVisits()).extracting(Visit::getDescription).containsExactly("rabies shot", "checkup");
        assertThat(olderVisit.getPet()).isSameAs(pet);
        assertThat(newerVisit.getDate()).isEqualTo(LocalDate.now().minusDays(1));
        assertThatThrownBy(() -> pet.getVisits().add(visit(3, LocalDate.now(), "blocked")))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void baseNamedEntityAndRolesShouldExposeSimpleValues() {
        PetType petType = petType(10, "cat");
        PetType newPetType = new PetType();
        Roles roles = new Roles();

        assertThat(petType.getId()).isEqualTo(10);
        assertThat(petType.isNew()).isFalse();
        assertThat(petType.getName()).isEqualTo("cat");
        assertThat(petType).hasToString("cat");
        assertThat(newPetType.isNew()).isTrue();
        assertThat(roles.OWNER_ADMIN).isEqualTo("ROLE_OWNER_ADMIN");
        assertThat(roles.VET_ADMIN).isEqualTo("ROLE_VET_ADMIN");
        assertThat(roles.ADMIN).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void exceptionAdviceShouldBuildGeneralAndDataIntegrityResponses() {
        ExceptionControllerAdvice advice = new ExceptionControllerAdvice();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/oops");

        ResponseEntity<ProblemDetail> generalResponse =
            advice.handleGeneralException(new RuntimeException("boom"), request);
        ResponseEntity<ProblemDetail> integrityResponse = advice.handleDataIntegrityViolationException(
            new DataIntegrityViolationException("constraint violation"),
            request);

        assertThat(generalResponse.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(generalResponse.getBody()).isNotNull();
        assertThat(generalResponse.getBody().getTitle()).isEqualTo("RuntimeException");
        assertThat(generalResponse.getBody().getDetail())
            .isEqualTo("An unexpected error occurred while processing your request");
        assertThat(generalResponse.getBody().getProperties()).containsKeys("timestamp", "schemaValidationErrors");
        assertThat(integrityResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(integrityResponse.getBody()).isNotNull();
        assertThat(integrityResponse.getBody().getDetail())
            .isEqualTo("The requested resource could not be processed due to a data constraint violation");
    }

    @Test
    void exceptionAdviceShouldBuildValidationResponseWithFieldDetails() throws Exception {
        ExceptionControllerAdvice advice = new ExceptionControllerAdvice();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new TestForm(), "testForm");
        bindingResult.rejectValue("name", "required", "must not be blank");
        MethodParameter parameter = new MethodParameter(
            SupportCodeCoverageTests.class.getDeclaredMethod("sampleValidatedMethod", TestForm.class),
            0);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/test");

        ResponseEntity<ProblemDetail> response = advice.handleMethodArgumentNotValidException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetail()).isEqualTo("The request contains invalid or missing parameters");
        assertThat(response.getBody().getProperties()).containsKey("schemaValidationErrors");
        assertThat(response.getBody().getProperties().get("schemaValidationErrors").toString())
            .contains("name", "must not be blank");
    }

    private static PetType petType(Integer id, String name) {
        PetType petType = new PetType();
        petType.setId(id);
        petType.setName(name);
        return petType;
    }

    private static Specialty specialty(Integer id, String name) {
        Specialty specialty = new Specialty();
        specialty.setId(id);
        specialty.setName(name);
        return specialty;
    }

    private static Pet pet(Integer id, String name, LocalDate birthDate) {
        Pet pet = new Pet();
        pet.setId(id);
        pet.setName(name);
        pet.setBirthDate(birthDate);
        pet.setType(petType(1, "dog"));
        return pet;
    }

    private static Visit visit(Integer id, LocalDate date, String description) {
        Visit visit = new Visit();
        visit.setId(id);
        visit.setDate(date);
        visit.setDescription(description);
        return visit;
    }

    @SuppressWarnings("unused")
    private void sampleValidatedMethod(TestForm form) {
    }

    private static class TestForm {

        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
