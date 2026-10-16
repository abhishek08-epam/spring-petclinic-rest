package org.springframework.samples.petclinic.teleconsultation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface TeleConsultationBookingRepository extends JpaRepository<TeleConsultationBooking, Long> {

    boolean existsByVetIdAndScheduledStartAndActiveTrue(Long vetId, Instant scheduledStart);

    @Query("SELECT tcb FROM TeleConsultationBooking tcb WHERE tcb.vet.id = :vetId AND tcb.scheduledStart >= :startTime AND tcb.scheduledStart < :endTime AND tcb.active = true")
    List<TeleConsultationBooking> findActiveBookingsByVetAndTimeRange(
        @Param("vetId") Long vetId,
        @Param("startTime") Instant startTime,
        @Param("endTime") Instant endTime
    );

    @Query("SELECT tcb FROM TeleConsultationBooking tcb WHERE tcb.pet.id = :petId ORDER BY tcb.requestedAt DESC")
    List<TeleConsultationBooking> findByPetIdOrderByRequestedAtDesc(@Param("petId") Long petId);
}