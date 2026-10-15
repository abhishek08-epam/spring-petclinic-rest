package org.springframework.samples.petclinic.repository.springdatajpa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

/**
 * Adapter that bridges the Spring Data JPA OwnerRepository to the generic OwnerRepository interface.
 * Active when 'spring-data-jpa' profile is enabled.
 */
@Repository
@Profile("spring-data-jpa")
public class SpringDataOwnerRepositoryImpl implements OwnerRepository {

    private final SpringDataOwnerRepository repository;

    @Autowired
    public SpringDataOwnerRepositoryImpl(SpringDataOwnerRepository repository) {
        this.repository = repository;
    }

    @Override
    public Owner findById(int id) throws DataAccessException {
        return repository.findById(id);
    }

    @Override
    public void save(Owner owner) throws DataAccessException {
        repository.save(owner);
    }

    @Override
    public Collection<Owner> findAll() throws DataAccessException {
        return repository.findAll();
    }

    @Override
    public void delete(Owner owner) throws DataAccessException {
        repository.delete(owner);
    }

    @Override
    public Page<Owner> findAll(Pageable pageable) throws DataAccessException {
        return repository.findAll(pageable);
    }

    @Override
    public Collection<Owner> findByLastName(String lastName) throws DataAccessException {
        return repository.findByLastName(lastName);
    }

    @Override
    public Page<Owner> findByLastName(String lastName, Pageable pageable) throws DataAccessException {
        return repository.findByLastName(lastName, pageable);
    }

    @Override
    public Collection<Owner> findByLastNameContainingIgnoreCase(String lastName) throws DataAccessException {
        return repository.findByLastNameContainingIgnoreCase(lastName);
    }

    @Override
    public Page<Owner> findByLastNameContainingIgnoreCase(String lastName, Pageable pageable) throws DataAccessException {
        return repository.findByLastNameContainingIgnoreCase(lastName, pageable);
    }
}
