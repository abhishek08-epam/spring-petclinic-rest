package org.springframework.samples.petclinic.repository.springdatajpa;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.samples.petclinic.model.Owner;

import java.util.Collection;

/**
 * Spring Data JPA specialization of the Owner Repository interface.
 * Active when 'spring-data-jpa' profile is enabled.
 */
@Profile("spring-data-jpa")
public interface SpringDataOwnerRepository extends Repository<Owner, Integer> {

    @Query("SELECT DISTINCT owner FROM Owner owner left join fetch owner.pets WHERE owner.id =:id")
    Owner findById(@Param("id") Integer id);

    void save(Owner owner);

    @Query("SELECT owner FROM Owner owner")
    Collection<Owner> findAll();

    void delete(Owner owner);

    @Query("SELECT owner FROM Owner owner")
    Page<Owner> findAll(Pageable pageable);

    @Query("SELECT DISTINCT owner FROM Owner owner left join fetch owner.pets WHERE owner.lastName LIKE :lastName%")
    Collection<Owner> findByLastName(@Param("lastName") String lastName);

    @Query("SELECT owner FROM Owner owner WHERE owner.lastName LIKE :lastName%")
    Page<Owner> findByLastName(@Param("lastName") String lastName, Pageable pageable);

    @Query("SELECT DISTINCT owner FROM Owner owner left join fetch owner.pets WHERE LOWER(owner.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))")
    Collection<Owner> findByLastNameContainingIgnoreCase(@Param("lastName") String lastName);

    @Query("SELECT owner FROM Owner owner WHERE LOWER(owner.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))")
    Page<Owner> findByLastNameContainingIgnoreCase(@Param("lastName") String lastName, Pageable pageable);
}
