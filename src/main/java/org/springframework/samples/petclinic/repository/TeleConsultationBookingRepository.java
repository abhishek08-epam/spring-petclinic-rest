/*
 * Copyright 2016-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.samples.petclinic.model.TeleConsultationBooking;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repository for TeleConsultationBooking entities.
 *
 * @author PetClinic Team
 */
@Repository
public interface TeleConsultationBookingRepository extends JpaRepository<TeleConsultationBooking, Integer> {

	/**
	 * Find bookings for a vet that overlap with the specified time range.
	 * Used for availability checking to prevent overbooking.
	 *
	 * @param vetId the vet ID
	 * @param startAt the start of the time range
	 * @param endAt the end of the time range
	 * @return list of overlapping bookings
	 */
	@Query("SELECT t FROM TeleConsultationBooking t WHERE t.vet.id = :vetId " +
		"AND t.status IN ('REQUESTED', 'CONFIRMED') " +
		"AND t.startAt < :endAt AND t.endAt > :startAt")
	List<TeleConsultationBooking> findOverlappingBookings(
		@Param("vetId") Integer vetId,
		@Param("startAt") Instant startAt,
		@Param("endAt") Instant endAt
	);

	/**
	 * Find all bookings for a specific pet, ordered by start time descending.
	 *
	 * @param petId the pet ID
	 * @return list of bookings for the pet
	 */
	@Query("SELECT t FROM TeleConsultationBooking t WHERE t.pet.id = :petId ORDER BY t.startAt DESC")
	List<TeleConsultationBooking> findByPetId(@Param("petId") Integer petId);

}
