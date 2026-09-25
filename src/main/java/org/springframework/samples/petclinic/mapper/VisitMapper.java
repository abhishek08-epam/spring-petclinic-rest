package org.springframework.samples.petclinic.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.samples.petclinic.dto.VisitDto;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.rest.dto.VisitFieldsDto;

import java.util.Collection;

/**
 * Map Visit & VisitDto using mapstruct
 */
@Mapper(uses = PetMapper.class)
public interface VisitMapper {
    @Mapping(source = "petId", target = "pet.id")
    Visit toVisit(org.springframework.samples.petclinic.rest.dto.VisitDto visitDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "pet", ignore = true)
    Visit toVisit(VisitFieldsDto visitFieldsDto);

    @Mapping(source = "pet.id", target = "petId")
    org.springframework.samples.petclinic.rest.dto.VisitDto toVisitDto(Visit visit);

    Collection<org.springframework.samples.petclinic.rest.dto.VisitDto> toVisitsDto(Collection<Visit> visits);

    @Mapping(source = "petId", target = "pet.id")
    Visit toVisit(VisitDto visitDto);

    @Mapping(source = "pet.id", target = "petId")
    VisitDto toVisitRecord(Visit visit);

}
