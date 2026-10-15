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
package org.springframework.samples.petclinic.repository;

import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

/**
 * Repository class for <code>Owner</code> domain objects.
 * All method names are compliant with Spring Data naming conventions,
 * so this interface can be used to automatically generate queries.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Michael Isvy
 */
public interface OwnerRepository extends Repository<Owner, Integer> {

    /**
     * Retrieve all <code>Owner</code>s from the data store.
     *
     * @return a <code>Collection</code> of <code>Owner</code>s
     */
    @Query("SELECT DISTINCT owner FROM Owner owner left join fetch owner.pets")
    @Transactional(readOnly = true)
    Collection<Owner> findAll() throws DataAccessException;

    /**
     * Retrieve all <code>Owner</code>s from the data store with pagination support.
     *
     * @param pageable pagination information
     * @return a <code>Page</code> of <code>Owner</code>s
     */
    @Query("SELECT owner FROM Owner owner")
    @Transactional(readOnly = true)
    Page<Owner> findAll(Pageable pageable) throws DataAccessException;

    /**
     * Retrieve an <code>Owner</code> from the data store by id.
     *
     * @param id the id to search for
     * @return the <code>Owner</code> if found
     */
    @Query("SELECT owner FROM Owner owner left join fetch owner.pets WHERE owner.id = :id")
    @Transactional(readOnly = true)
    Owner findById(@Param("id") Integer id) throws DataAccessException;

    /**
     * Save an <code>Owner</code> to the data store, either inserting or updating it.
     *
     * @param owner the <code>Owner</code> to save
     */
    void save(Owner owner) throws DataAccessException;

    /**
     * Retrieve <code>Owner</code>s from the data store by last name,
     * returning all owners whose last name <i>starts with</i> or exactly matches the given name.
     *
     * @param lastName Value to search for
     * @return a <code>Collection</code> of matching <code>Owner</code>s (or an empty <code>Collection</code> if none found)
     */
    @Query("SELECT DISTINCT owner FROM Owner owner left join fetch owner.pets WHERE owner.lastName LIKE :lastName%")
    @Transactional(readOnly = true)
    Collection<Owner> findByLastName(@Param("lastName") String lastName) throws DataAccessException;

    /**
     * Retrieve <code>Owner</code>s from the data store whose last name contains the given substring,
     * ignoring case.
     *
     * @param lastName Substring to search for (case-insensitive)
     * @return a <code>Collection</code> of matching <code>Owner</code>s (or an empty <code>Collection</code> if none found)
     */
    @EntityGraph(attributePaths = "pets")
    @Transactional(readOnly = true)
    Collection<Owner> findByLastNameContainingIgnoreCase(String lastName) throws DataAccessException;

    /**
     * Retrieve <code>Owner</code>s from the data store whose last name contains the given substring,
     * ignoring case, with pagination support.
     *
     * @param lastName Substring to search for (case-insensitive)
     * @param pageable Pagination information
     * @return a <code>Page</code> of matching <code>Owner</code>s (or an empty <code>Page</code> if none found)
     */
    @Transactional(readOnly = true)
    Page<Owner> findByLastNameContainingIgnoreCase(String lastName, Pageable pageable) throws DataAccessException;

    /**
     * Delete an <code>Owner</code> from the data store by <code>Owner</code>.
     *
     * @param owner the <code>Owner</code> to delete
     */
    void delete(Owner owner) throws DataAccessException;
}
