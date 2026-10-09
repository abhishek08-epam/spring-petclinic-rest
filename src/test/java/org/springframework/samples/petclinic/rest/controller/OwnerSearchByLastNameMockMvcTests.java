package org.springframework.samples.petclinic.rest.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.mapper.VisitMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.advice.ExceptionControllerAdvice;
import org.springframework.samples.petclinic.rest.controller.v1.OwnerRestControllerV1;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.samples.petclinic.service.clinicService.ApplicationTestConfig;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ContextConfiguration(classes = ApplicationTestConfig.class)
@WebAppConfiguration
class OwnerSearchByLastNameMockMvcTests {

    @Autowired
    private OwnerRestControllerV1 ownerRestControllerV1;

    @Autowired
    private OwnerMapper ownerMapper;

    @Autowired
    private PetMapper petMapper;

    @Autowired
    private VisitMapper visitMapper;

    @MockitoBean
    private ClinicService clinicService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(ownerRestControllerV1)
            .setControllerAdvice(new ExceptionControllerAdvice())
            .build();
    }

    static Stream<Arguments> happyPathSearches() {
        return Stream.of(
            Arguments.of("Exact match", "Davis", owner(2, "Betty", "Davis"), owner(4, "Harold", "Davis")),
            Arguments.of("Partial match (case-insensitive)", "daV", owner(2, "Betty", "Davis"), owner(4, "Harold", "Davis")),
            Arguments.of("Special characters in input", "O'Con", owner(7, "Mary", "O'Connor"))
        );
    }

    static Stream<Arguments> notFoundSearches() {
        return Stream.of(
            Arguments.of("No results found", "DoesNotExist"),
            Arguments.of("Special characters no match", "@#$%")
        );
    }

    static Stream<Arguments> invalidSearches() {
        return Stream.of(
            Arguments.of("Missing parameter", null),
            Arguments.of("Empty", ""),
            Arguments.of("Blank", "   ")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("happyPathSearches")
    @WithMockUser(roles = "OWNER_ADMIN")
    @DisplayName("GET /api/owners/search?lastName=... should return 200 with results")
    void searchOwners_happyPaths(String displayName, String lastName, Owner... foundOwners) throws Exception {
        given(this.clinicService.searchOwnersByLastName(lastName)).willReturn(List.of(foundOwners));

        ResultActions result = this.mockMvc.perform(get("/api/owners/search").queryParam("lastName", lastName)
            .accept(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(foundOwners.length));

        for (int i = 0; i < foundOwners.length; i++) {
            Owner o = foundOwners[i];
            result.andExpect(jsonPath("$[" + i + "].id").value(o.getId()))
                .andExpect(jsonPath("$[" + i + "].firstName").value(o.getFirstName()))
                .andExpect(jsonPath("$[" + i + "].lastName").value(o.getLastName()))
                .andExpect(jsonPath("$[" + i + "].address").value(o.getAddress()))
                .andExpect(jsonPath("$[" + i + "].city").value(o.getCity()))
                .andExpect(jsonPath("$[" + i + "].telephone").value(o.getTelephone()));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("notFoundSearches")
    @WithMockUser(roles = "OWNER_ADMIN")
    @DisplayName("GET /api/owners/search?lastName=... should return 404 when no owners found")
    void searchOwners_notFound(String displayName, String lastName) throws Exception {
        given(this.clinicService.searchOwnersByLastName(lastName)).willReturn(List.of());

        this.mockMvc.perform(get("/api/owners/search").queryParam("lastName", lastName)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidSearches")
    @WithMockUser(roles = "OWNER_ADMIN")
    @DisplayName("GET /api/owners/search should validate lastName and return 400 on invalid input")
    void searchOwners_invalidInput_returnsBadRequest(String displayName, String lastName) throws Exception {
        ResultActions result;
        if (lastName == null) {
            result = this.mockMvc.perform(get("/api/owners/search").accept(MediaType.APPLICATION_JSON));
        } else {
            result = this.mockMvc.perform(get("/api/owners/search").queryParam("lastName", lastName)
                .accept(MediaType.APPLICATION_JSON));
        }

        result.andExpect(status().isBadRequest());

        verify(this.clinicService, never()).searchOwnersByLastName(anyString());
    }

    private static Owner owner(int id, String firstName, String lastName) {
        Owner owner = new Owner();
        owner.setId(id);
        owner.setFirstName(firstName);
        owner.setLastName(lastName);
        owner.setAddress("addr-" + id);
        owner.setCity("city-" + id);
        owner.setTelephone("000000000" + (id % 10));
        return owner;
    }
}
