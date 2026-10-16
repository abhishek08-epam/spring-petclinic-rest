package org.springframework.samples.petclinic.repository;

import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.model.Owner;

import java.util.Collection;

/**
 * Repository interface for Owner domain objects.
 * All method definitions require DataAccessException to be thrown.
 */
public interface OwnerRepository {

    /**
     * Retrieve an Owner by its id.
     * @param id the id to search for
     * @return the Owner if found
     * @throws DataAccessException if the id is not found
     */
    Owner findById(int id) throws DataAccessException;

    /**
     * Save an Owner to the data store, either inserting or updating it.
     * @param owner the Owner to save
     * @throws DataAccessException on database error
     */
    void save(Owner owner) throws DataAccessException;

    /**
     * Retrieve all Owners from the data store.
     * @return a Collection of Owners
     * @throws DataAccessException on database error
     */
    Collection<Owner> findAll() throws DataAccessException;

    /**
     * Delete an Owner from the data store.
     * @param owner the Owner to delete
     * @throws DataAccessException on database error
     */
    void delete(Owner owner) throws DataAccessException;

    /**
     * Retrieve all Owners with pagination.
     * @param pageable pagination information
     * @return a Page of Owners
     * @throws DataAccessException on database error
     */
    Page<Owner> findAll(Pageable pageable) throws DataAccessException;

    /**
     * Retrieve Owners whose last name starts with the given string.
     * @param lastName the prefix to search for
     * @return a Collection of matching Owners
     * @throws DataAccessException on database error
     */
    Collection<Owner> findByLastName(String lastName) throws DataAccessException;

    /**
     * Retrieve Owners whose last name starts with the given string, with pagination.
     * @param lastName the prefix to search for
     * @param pageable pagination information
     * @return a Page of matching Owners
     * @throws DataAccessException on database error
     */
    Page<Owner> findByLastName(String lastName, Pageable pageable) throws DataAccessException;

    /**
     * Retrieve Owners whose last name contains the given substring (case-insensitive).
     * Supports partial matching anywhere in the last name field.
     * @param lastName the substring to search for (case-insensitive)
     * @return a Collection of matching Owners (empty if no matches)
     * @throws DataAccessException on database error
     */
    Collection<Owner> findByLastNameContainingIgnoreCase(String lastName) throws DataAccessException;

    /**
     * Retrieve Owners whose last name contains the given substring (case-insensitive), with pagination.
     * Supports partial matching anywhere in the last name field.
     * @param lastName the substring to search for (case-insensitive)
     * @param pageable pagination information
     * @return a Page of matching Owners (empty if no matches)
     * @throws DataAccessException on database error
     */
    Page<Owner> findByLastNameContainingIgnoreCase(String lastName, Pageable pageable) throws DataAccessException;
}
