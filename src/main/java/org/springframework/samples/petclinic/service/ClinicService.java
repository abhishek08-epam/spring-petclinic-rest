/*
 * Copyright 2002-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.service;

import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.model.*;

import java.util.Collection;

/**
 * Mostly used as a facade to define all the business operations
 * that Petclinic provides in terms of veterinarians, owners, and their pets.
 *
 * @author Michael Isvy
 * @author Vitaliy Fedoriv
 */
public interface ClinicService {

    Owner findOwnerById(int id) throws DataAccessException;

    Collection<Owner> findAllOwners() throws DataAccessException;

    Page<Owner> findAllOwners(Pageable pageable) throws DataAccessException;

    void saveOwner(Owner owner) throws DataAccessException;

    void deleteOwner(Owner owner) throws DataAccessException;

    Collection<Owner> findOwnerByLastName(String lastName) throws DataAccessException;

    /**
     * Find owners by last name using case-insensitive partial matching.
     *
     * @param lastName Substring to search for (case-insensitive); null or blank returns empty list
     * @return Collection of matching owners, or empty list if none found or input is null/blank
     * @throws DataAccessException if there is a data access error
     */
    Collection<Owner> findOwnerByLastNameContaining(String lastName) throws DataAccessException;

    /**
     * Find owners by last name using case-insensitive partial matching, with pagination.
     *
     * @param lastName Substring to search for (case-insensitive); null or blank returns empty page
     * @param pageable Pagination information
     * @return Page of matching owners, or empty page if none found or input is null/blank
     * @throws DataAccessException if there is a data access error
     */
    Page<Owner> findOwnerByLastNameContaining(String lastName, Pageable pageable) throws DataAccessException;

    Pet findPetById(int id) throws DataAccessException;

    Collection<Pet> findAllPets() throws DataAccessException;

    Page<Pet> findAllPets(Pageable pageable) throws DataAccessException;

    void savePet(Pet pet) throws DataAccessException;

    void deletePet(Pet pet) throws DataAccessException;

    Collection<Visit> findAllVisits() throws DataAccessException;

    Page<Visit> findAllVisits(Pageable pageable) throws DataAccessException;

    Visit findVisitById(int visitId) throws DataAccessException;

    void saveVisit(Visit visit) throws DataAccessException;

    void deleteVisit(Visit visit) throws DataAccessException;

    Vet findVetById(int id) throws DataAccessException;

    Collection<Vet> findAllVets() throws DataAccessException;

    Page<Vet> findAllVets(Pageable pageable) throws DataAccessException;

    void saveVet(Vet vet) throws DataAccessException;

    void deleteVet(Vet vet) throws DataAccessException;

    Collection<PetType> findAllPetTypes() throws DataAccessException;

    Page<PetType> findAllPetTypes(Pageable pageable) throws DataAccessException;

    PetType findPetTypeById(int petTypeId) throws DataAccessException;

    void savePetType(PetType petType) throws DataAccessException;

    void deletePetType(PetType petType) throws DataAccessException;

    Collection<Specialty> findAllSpecialties() throws DataAccessException;

    Specialty findSpecialtyById(int specialtyId) throws DataAccessException;

    void saveSpecialty(Specialty specialty) throws DataAccessException;

    void deleteSpecialty(Specialty specialty) throws DataAccessException;

    Collection<Visit> findVisitsByPetId(int petId) throws DataAccessException;
}
