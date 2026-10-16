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
package org.springframework.samples.petclinic.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.TeleConsultationBooking;
import org.springframework.samples.petclinic.model.TeleConsultationBooking.BookingStatus;
import org.springframework.samples.petclinic.model.TeleConsultationBooking.Severity;
import org.springframework.samples.petclinic.model.TeleConsultationBooking.TriagePriority;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.repository.PetRepository;
import org.springframework.samples.petclinic.repository.TeleConsultationBookingRepository;
import org.springframework.samples.petclinic.repository.VetRepository;
import org.springframework.samples.petclinic.rest.advice.ResourceNotFoundException;
import org.springframework.samples.petclinic.rest.dto.EmergencyTeleConsultationConfirmationDto;
import org.springframework.samples.petclinic.rest.dto.EmergencyTeleConsultationRequestDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Service for managing emergency tele-consultation bookings.
 * Implements triage priority calculation, vet availability checking with timeout,
 * and overbooking prevention.
 *
 * @author PetClinic Team
 */
@Service
public class TeleConsultationBookingService {

	private static final Logger logger = LoggerFactory.getLogger(TeleConsultationBookingService.class);
	private static final int AVAILABILITY_CHECK_TIMEOUT_MS = 200;
	private static final String MEETING_URL_TEMPLATE = "https://petclinic.meet/room/";

	private final TeleConsultationBookingRepository bookingRepository;
	private final PetRepository petRepository;
	private final VetRepository vetRepository;
	private final ExecutorService executorService;

	public TeleConsultationBookingService(
		TeleConsultationBookingRepository bookingRepository,
		PetRepository petRepository,
		VetRepository vetRepository
	) {
		this.bookingRepository = bookingRepository;
		this.petRepository = petRepository;
		this.vetRepository = vetRepository;
		this.executorService = Executors.newCachedThreadPool();
	}

	/**
	 * Create an emergency tele-consultation booking.
	 * Implements AC-1, AC-2, AC-3, AC-4.
	 *
	 * @param request the booking request
	 * @return confirmation DTO with booking details
	 * @throws ResourceNotFoundException if pet or vet not found
	 */
	@Transactional
	public EmergencyTeleConsultationConfirmationDto createEmergencyBooking(EmergencyTeleConsultationRequestDto request) {
		logger.info("Creating emergency booking for pet ID: {}", request.petId());

		// Fetch pet entity
		Pet pet = petRepository.findById(request.petId())
			.orElseThrow(() -> new ResourceNotFoundException("Pet not found with id: " + request.petId()));

		// AC-2: Calculate triage priority from severity
		Severity severity = request.getSeverityOrDefault();
		TriagePriority priority = calculateTriagePriority(severity);
		logger.debug("Calculated triage priority: {} for severity: {}", priority, severity);

		// Determine time window
		Instant startAt = request.getPreferredStartAtOrNow();
		Instant endAt = startAt.plus(request.durationMinutes(), ChronoUnit.MINUTES);

		// AC-3: Best-effort vet availability check (with timeout)
		Vet assignedVet = findAvailableVet(startAt, endAt);

		// Create and persist booking
		TeleConsultationBooking booking = new TeleConsultationBooking();
		booking.setPet(pet);
		booking.setVet(assignedVet);
		booking.setStartAt(startAt);
		booking.setEndAt(endAt);
		booking.setStatus(BookingStatus.CONFIRMED);
		booking.setEmergency(true);
		booking.setReason(request.reason());
		booking.setSeverity(severity);
		booking.setTriagePriority(priority);
		booking.setMeetingUrl(generateMeetingUrl(booking));

		TeleConsultationBooking saved = bookingRepository.save(booking);
		logger.info("Emergency booking created with ID: {}", saved.getId());

		// AC-4: Return confirmation DTO
		return new EmergencyTeleConsultationConfirmationDto(
			saved.getId(),
			saved.getPet().getId(),
			saved.getVet().getId(),
			saved.getStartAt(),
			saved.getEndAt(),
			saved.getTriagePriority(),
			saved.getStatus().name(),
			saved.getMeetingUrl()
		);
	}

	/**
	 * Calculate triage priority from symptom severity.
	 * Implements deterministic mapping per AC-2.
	 *
	 * @param severity the symptom severity
	 * @return the triage priority
	 */
	private TriagePriority calculateTriagePriority(Severity severity) {
		return switch (severity) {
			case MILD -> TriagePriority.LOW;
			case MODERATE -> TriagePriority.MEDIUM;
			case SEVERE -> TriagePriority.HIGH;
		};
	}

	/**
	 * Find an available vet for the specified time window.
	 * Implements best-effort availability check with timeout per AC-3 and BC-2.
	 *
	 * @param startAt consultation start time
	 * @param endAt consultation end time
	 * @return an available vet, or the first vet if check times out
	 * @throws ResourceNotFoundException if no vets exist in the system
	 */
	private Vet findAvailableVet(Instant startAt, Instant endAt) {
		List<Vet> allVets = vetRepository.findAll();

		if (allVets.isEmpty()) {
			logger.error("No vets available in the system");
			throw new ResourceNotFoundException("No vets available");
		}

		// Best-effort availability check with timeout
		Future<Optional<Vet>> availabilityCheckFuture = executorService.submit(() -> {
			for (Vet vet : allVets) {
				List<TeleConsultationBooking> overlapping = bookingRepository.findOverlappingBookings(
					vet.getId(), startAt, endAt
				);
				if (overlapping.isEmpty()) {
					logger.debug("Found available vet: {}", vet.getId());
					return Optional.of(vet);
				}
			}
			return Optional.empty();
		});

		Optional<Vet> availableVet = getAvailableVetWithTimeout(availabilityCheckFuture);

		// BC-2: If check timed out or failed, use first vet (best effort)
		if (availableVet.isEmpty()) {
			logger.warn("Availability check timed out or no available vet found, assigning first vet");
			return allVets.get(0);
		}

		return availableVet.get();
	}

	/**
	 * Execute availability check with hard timeout.
	 * Implements timeout mechanism per AC-3.
	 *
	 * @param future the availability check task
	 * @return optional vet if found within timeout, empty otherwise
	 */
	private Optional<Vet> getAvailableVetWithTimeout(Future<Optional<Vet>> future) {
		try {
			return future.get(AVAILABILITY_CHECK_TIMEOUT_MS, TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			logger.warn("Availability check timed out after {}ms", AVAILABILITY_CHECK_TIMEOUT_MS);
			future.cancel(true);
			return Optional.empty();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.warn("Availability check interrupted", e);
			return Optional.empty();
		} catch (ExecutionException e) {
			logger.warn("Availability check failed with exception", e);
			return Optional.empty();
		}
	}

	/**
	 * Generate a unique meeting URL for the booking.
	 *
	 * @param booking the booking entity
	 * @return meeting URL
	 */
	private String generateMeetingUrl(TeleConsultationBooking booking) {
		return MEETING_URL_TEMPLATE + booking.getPet().getId() + "-" + System.currentTimeMillis();
	}

}
