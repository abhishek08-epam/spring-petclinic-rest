package org.springframework.samples.petclinic.repository.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.repository.PetRepository;
import org.springframework.samples.petclinic.repository.VisitRepository;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.*;

/**
 * JDBC implementation of the OwnerRepository interface.
 * Active when 'jdbc' profile is enabled.
 */
@Repository
@Profile("jdbc")
public class JdbcOwnerRepositoryImpl implements OwnerRepository {

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private SimpleJdbcInsert insertOwner;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private VisitRepository visitRepository;

    @Autowired
    public JdbcOwnerRepositoryImpl(DataSource dataSource, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.insertOwner = new SimpleJdbcInsert(dataSource)
            .withTableName("owners")
            .usingGeneratedKeyColumns("id");
    }

    @Override
    public Collection<Owner> findByLastName(String lastName) throws DataAccessException {
        Map<String, Object> params = new HashMap<>();
        params.put("lastName", lastName + "%");
        List<Owner> owners = this.namedParameterJdbcTemplate.query(
            "SELECT id, first_name, last_name, address, city, telephone FROM owners WHERE last_name LIKE :lastName",
            params,
            BeanPropertyRowMapper.newInstance(Owner.class)
        );
        loadOwnersPetsAndVisits(owners);
        return owners;
    }

    @Override
    public Page<Owner> findByLastName(String lastName, Pageable pageable) throws DataAccessException {
        Map<String, Object> params = new HashMap<>();
        params.put("lastName", lastName + "%");
        params.put("limit", pageable.getPageSize());
        params.put("offset", pageable.getOffset());

        List<Owner> owners = this.namedParameterJdbcTemplate.query(
            "SELECT id, first_name, last_name, address, city, telephone FROM owners WHERE last_name LIKE :lastName LIMIT :limit OFFSET :offset",
            params,
            BeanPropertyRowMapper.newInstance(Owner.class)
        );

        Integer total = this.namedParameterJdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM owners WHERE last_name LIKE :lastName",
            Map.of("lastName", lastName + "%"),
            Integer.class
        );

        return new PageImpl<>(owners, pageable, total != null ? total : 0);
    }

    @Override
    public Collection<Owner> findByLastNameContainingIgnoreCase(String lastName) throws DataAccessException {
        Map<String, Object> params = new HashMap<>();
        params.put("lastName", "%" + lastName + "%");
        List<Owner> owners = this.namedParameterJdbcTemplate.query(
            "SELECT id, first_name, last_name, address, city, telephone FROM owners WHERE LOWER(last_name) LIKE LOWER(:lastName)",
            params,
            BeanPropertyRowMapper.newInstance(Owner.class)
        );
        loadOwnersPetsAndVisits(owners);
        return owners;
    }

    @Override
    public Page<Owner> findByLastNameContainingIgnoreCase(String lastName, Pageable pageable) throws DataAccessException {
        Map<String, Object> params = new HashMap<>();
        params.put("lastName", "%" + lastName + "%");
        params.put("limit", pageable.getPageSize());
        params.put("offset", pageable.getOffset());

        List<Owner> owners = this.namedParameterJdbcTemplate.query(
            "SELECT id, first_name, last_name, address, city, telephone FROM owners WHERE LOWER(last_name) LIKE LOWER(:lastName) LIMIT :limit OFFSET :offset",
            params,
            BeanPropertyRowMapper.newInstance(Owner.class)
        );

        Integer total = this.namedParameterJdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM owners WHERE LOWER(last_name) LIKE LOWER(:lastName)",
            Map.of("lastName", "%" + lastName + "%"),
            Integer.class
        );

        return new PageImpl<>(owners, pageable, total != null ? total : 0);
    }

    @Override
    public Owner findById(int id) throws DataAccessException {
        Owner owner;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("id", id);
            owner = this.namedParameterJdbcTemplate.queryForObject(
                "SELECT id, first_name, last_name, address, city, telephone FROM owners WHERE id = :id",
                params,
                BeanPropertyRowMapper.newInstance(Owner.class));
        } catch (EmptyResultDataAccessException ex) {
            throw new ObjectRetrievalFailureException(Owner.class, id);
        }
        loadPetsAndVisits(owner);
        return owner;
    }

    @Override
    public void save(Owner owner) throws DataAccessException {
        BeanPropertySqlParameterSource parameterSource = new BeanPropertySqlParameterSource(owner);
        if (owner.isNew()) {
            Number newKey = this.insertOwner.executeAndReturnKey(parameterSource);
            owner.setId(newKey.intValue());
        } else {
            this.namedParameterJdbcTemplate.update(
                "UPDATE owners SET first_name=:firstName, last_name=:lastName, address=:address, city=:city, telephone=:telephone WHERE id=:id",
                parameterSource);
        }
    }

    @Override
    public Collection<Owner> findAll() throws DataAccessException {
        List<Owner> owners = this.namedParameterJdbcTemplate.query(
            "SELECT id, first_name, last_name, address, city, telephone FROM owners",
            BeanPropertyRowMapper.newInstance(Owner.class));
        return owners;
    }

    @Override
    public void delete(Owner owner) throws DataAccessException {
        Map<String, Object> params = new HashMap<>();
        params.put("id", owner.getId());
        List<Pet> pets = new ArrayList<>(petRepository.findByOwnerId(owner.getId()));
        for (Pet pet : pets) {
            List<Visit> visits = visitRepository.findByPetId(pet.getId());
            for (Visit visit : visits) {
                visitRepository.delete(visit);
            }
            petRepository.delete(pet);
        }
        this.namedParameterJdbcTemplate.update("DELETE FROM owners WHERE id=:id", params);
    }

    @Override
    public Page<Owner> findAll(Pageable pageable) throws DataAccessException {
        Map<String, Object> params = new HashMap<>();
        params.put("limit", pageable.getPageSize());
        params.put("offset", pageable.getOffset());

        List<Owner> owners = this.namedParameterJdbcTemplate.query(
            "SELECT id, first_name, last_name, address, city, telephone FROM owners LIMIT :limit OFFSET :offset",
            params,
            BeanPropertyRowMapper.newInstance(Owner.class)
        );

        Integer total = this.namedParameterJdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM owners",
            new HashMap<>(),
            Integer.class
        );

        return new PageImpl<>(owners, pageable, total != null ? total : 0);
    }

    private void loadOwnersPetsAndVisits(Collection<Owner> owners) {
        for (Owner owner : owners) {
            loadPetsAndVisits(owner);
        }
    }

    private void loadPetsAndVisits(Owner owner) {
        List<Pet> pets = this.petRepository.findByOwnerId(owner.getId());
        for (Pet pet : pets) {
            owner.addPet(pet);
            pet.setVisitsInternal(visitRepository.findByPetId(pet.getId()));
        }
    }
}
