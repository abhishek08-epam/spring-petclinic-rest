package org.springframework.samples.petclinic.service;

import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.model.Specialty;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.model.Visit;

import java.util.Collection;

/**
 * Business service interface for PetClinic operations.
 */
public interface ClinicService {

    Pet findPetById(int id) throws DataAccessException;

    Collection<Pet> findAllPets() throws DataAccessException;

    void savePet(Pet pet) throws DataAccessException;

    void deletePet(Pet pet) throws DataAccessException;

    Collection<Visit> findVisitsByPetId(int petId);

    Visit findVisitById(int visitId) throws DataAccessException;

    Collection<Visit> findAllVisits() throws DataAccessException;

    void saveVisit(Visit visit) throws DataAccessException;

    void deleteVisit(Visit visit) throws DataAccessException;

    Vet findVetById(int id) throws DataAccessException;

    Collection<Vet> findAllVets() throws DataAccessException;

    void saveVet(Vet vet) throws DataAccessException;

    void deleteVet(Vet vet) throws DataAccessException;

    Owner findOwnerById(int id) throws DataAccessException;

    Page<Owner> findOwnerByLastName(String lastName, Pageable pageable) throws DataAccessException;

    Collection<Owner> findOwnerByLastName(String lastName) throws DataAccessException;

    Collection<Owner> findAllOwners() throws DataAccessException;

    Page<Owner> findAllOwners(Pageable pageable) throws DataAccessException;

    void saveOwner(Owner owner) throws DataAccessException;

    void deleteOwner(Owner owner) throws DataAccessException;

    Collection<PetType> findAllPetTypes() throws DataAccessException;

    PetType findPetTypeById(int petTypeId);

    void savePetType(PetType petType) throws DataAccessException;

    void deletePetType(PetType petType) throws DataAccessException;

    Specialty findSpecialtyById(int specialtyId);

    Collection<Specialty> findAllSpecialties() throws DataAccessException;

    void saveSpecialty(Specialty specialty) throws DataAccessException;

    void deleteSpecialty(Specialty specialty) throws DataAccessException;

    /**
     * Search for Owners whose last name contains the given substring (case-insensitive).
     * Returns an empty list if lastName is null or blank.
     * @param lastName the search term (trimmed before search)
     * @return a Collection of matching Owners (never null, empty if no matches or invalid input)
     * @throws DataAccessException on database error
     */
    Collection<Owner> searchOwnersByLastName(String lastName) throws DataAccessException;

    /**
     * Search for Owners whose last name contains the given substring (case-insensitive), with pagination.
     * Returns an empty page if lastName is null or blank.
     * @param lastName the search term (trimmed before search)
     * @param pageable pagination information
     * @return a Page of matching Owners (never null, empty if no matches or invalid input)
     * @throws DataAccessException on database error
     */
    Page<Owner> searchOwnersByLastName(String lastName, Pageable pageable) throws DataAccessException;
}
