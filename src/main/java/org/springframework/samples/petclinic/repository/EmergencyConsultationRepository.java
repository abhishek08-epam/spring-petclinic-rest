package org.springframework.samples.petclinic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.samples.petclinic.model.EmergencyConsultation;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface EmergencyConsultationRepository extends JpaRepository<EmergencyConsultation, Long> {

    @Query("SELECT CASE WHEN COUNT(ec) > 0 THEN true ELSE false END FROM EmergencyConsultation ec WHERE ec.vet.id = :vetId AND ec.scheduledTime = :scheduledTime")
    boolean existsByVetIdAndScheduledTime(@Param("vetId") Long vetId, @Param("scheduledTime") Instant scheduledTime);
}
