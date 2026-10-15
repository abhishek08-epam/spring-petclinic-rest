package org.springframework.samples.petclinic.repository.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * JPA implementation of the OwnerRepository interface.
 * Active when 'jpa' profile is enabled.
 */
@Repository
@Profile("jpa")
public class JpaOwnerRepositoryImpl implements OwnerRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    @SuppressWarnings("unchecked")
    public Collection<Owner> findByLastName(String lastName) throws DataAccessException {
        Query query = this.em.createQuery("SELECT DISTINCT owner FROM Owner owner left join fetch owner.pets WHERE owner.lastName LIKE :lastName");
        query.setParameter("lastName", lastName + "%");
        return query.getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Page<Owner> findByLastName(String lastName, Pageable pageable) throws DataAccessException {
        Query query = this.em.createQuery("SELECT owner FROM Owner owner WHERE owner.lastName LIKE :lastName");
        query.setParameter("lastName", lastName + "%");
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<Owner> owners = query.getResultList();

        Query countQuery = this.em.createQuery("SELECT COUNT(owner.id) FROM Owner owner WHERE owner.lastName LIKE :lastName");
        countQuery.setParameter("lastName", lastName + "%");
        Long count = (Long) countQuery.getSingleResult();

        return new PageImpl<>(owners, pageable, count);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<Owner> findByLastNameContainingIgnoreCase(String lastName) throws DataAccessException {
        Query query = this.em.createQuery("SELECT DISTINCT owner FROM Owner owner left join fetch owner.pets WHERE LOWER(owner.lastName) LIKE LOWER(:lastName)");
        query.setParameter("lastName", "%" + lastName + "%");
        return query.getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Page<Owner> findByLastNameContainingIgnoreCase(String lastName, Pageable pageable) throws DataAccessException {
        Query query = this.em.createQuery("SELECT owner FROM Owner owner WHERE LOWER(owner.lastName) LIKE LOWER(:lastName)");
        query.setParameter("lastName", "%" + lastName + "%");
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<Owner> owners = query.getResultList();

        Query countQuery = this.em.createQuery("SELECT COUNT(owner.id) FROM Owner owner WHERE LOWER(owner.lastName) LIKE LOWER(:lastName)");
        countQuery.setParameter("lastName", "%" + lastName + "%");
        Long count = (Long) countQuery.getSingleResult();

        return new PageImpl<>(owners, pageable, count);
    }

    @Override
    public Owner findById(int id) throws DataAccessException {
        Query query = this.em.createQuery("SELECT owner FROM Owner owner left join fetch owner.pets WHERE owner.id =:id");
        query.setParameter("id", id);
        return (Owner) query.getSingleResult();
    }

    @Override
    public void save(Owner owner) throws DataAccessException {
        if (owner.getId() == null) {
            this.em.persist(owner);
        } else {
            this.em.merge(owner);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<Owner> findAll() throws DataAccessException {
        Query query = this.em.createQuery("SELECT owner FROM Owner owner");
        return query.getResultList();
    }

    @Override
    public void delete(Owner owner) throws DataAccessException {
        this.em.remove(this.em.contains(owner) ? owner : this.em.merge(owner));
        Integer ownerId = owner.getId();
        Query query = this.em.createQuery("DELETE FROM Visit visit WHERE visit.pet.id IN (SELECT pet.id FROM Pet pet WHERE pet.owner.id = :ownerId)");
        query.setParameter("ownerId", ownerId);
        query.executeUpdate();
        query = this.em.createQuery("DELETE FROM Pet pet WHERE pet.owner.id = :ownerId");
        query.setParameter("ownerId", ownerId);
        query.executeUpdate();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Page<Owner> findAll(Pageable pageable) throws DataAccessException {
        Query query = this.em.createQuery("SELECT owner FROM Owner owner");
        int total = query.getResultList().size();
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<Owner> owners = query.getResultList();
        return new PageImpl<>(owners, pageable, total);
    }
}
