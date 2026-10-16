package org.springframework.samples.petclinic.teleconsultation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetRepository;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;

@Service
public class TeleConsultationBookingService {

    private static final Logger logger = LoggerFactory.getLogger(TeleConsultationBookingService.class);
    private static final int DEFAULT_CONSULTATION_DURATION_MINUTES = 30;

    private final TeleConsultationBookingRepository bookingRepository;
    private final PetRepository petRepository;
    private final VetRepository vetRepository;
    private final ExecutorService vetAvailabilityExecutor;
    private final long vetCheckTimeoutMs;

    public TeleConsultationBookingService(
        TeleConsultationBookingRepository bookingRepository,
        PetRepository petRepository,
        VetRepository vetRepository,
        @Value("${teleconsultation.vet-check-timeout-ms:300}") long vetCheckTimeoutMs
    ) {
        this.bookingRepository = bookingRepository;
        this.petRepository = petRepository;
        this.vetRepository = vetRepository;
        this.vetCheckTimeoutMs = vetCheckTimeoutMs;
        this.vetAvailabilityExecutor = Executors.newVirtualThreadPerTaskExecutor();
    }

    @Transactional
    public EmergencyTeleConsultationConfirmation createEmergencyBooking(EmergencyTeleConsultationRequest request) {
        Pet pet = petRepository.findById(request.petId())
            .orElseThrow(() -> new PetNotFoundException(request.petId()));

        TeleConsultationBooking booking = new TeleConsultationBooking();
        booking.setPet(pet);
        booking.setSeverity(request.severity());
        booking.setTriageLevel(calculateTriageLevel(request.severity()));
        booking.setSymptoms(request.symptoms());
        booking.setChannel(request.channel());
        booking.setRequestedAt(Instant.now());
        booking.setBookingReference(generateBookingReference());
        booking.setActive(true);

        Instant scheduledStart = determineScheduledStart(request);
        booking.setScheduledStart(scheduledStart);

        if (scheduledStart != null) {
            booking.setScheduledEnd(scheduledStart.plus(Duration.ofMinutes(DEFAULT_CONSULTATION_DURATION_MINUTES)));
        }

        Optional<Vet> availableVet = findAvailableVet(scheduledStart);

        if (availableVet.isPresent()) {
            booking.setVet(availableVet.get());
            booking.setStatus(TeleConsultationBooking.BookingStatus.SCHEDULED);
        } else {
            booking.setStatus(TeleConsultationBooking.BookingStatus.TRIAGED);
        }

        TeleConsultationBooking saved = persistBooking(booking);

        return mapToConfirmation(saved);
    }

    private TeleConsultationBooking persistBooking(TeleConsultationBooking booking) {
        try {
            return bookingRepository.save(booking);
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage() != null && e.getMessage().contains("uk_vet_slot_active")) {
                throw new VetSlotConflictException("Selected vet slot is no longer available");
            }
            throw e;
        }
    }

    private TeleConsultationBooking.TriageLevel calculateTriageLevel(TeleConsultationBooking.Severity severity) {
        return switch (severity) {
            case MILD -> TeleConsultationBooking.TriageLevel.GREEN;
            case MODERATE -> TeleConsultationBooking.TriageLevel.YELLOW;
            case SEVERE -> TeleConsultationBooking.TriageLevel.RED;
        };
    }

    private Instant determineScheduledStart(EmergencyTeleConsultationRequest request) {
        if (request.requestedStart() != null) {
            return request.requestedStart();
        }
        return Instant.now().plus(Duration.ofHours(1));
    }

    private Optional<Vet> findAvailableVet(Instant scheduledStart) {
        if (scheduledStart == null) {
            return Optional.empty();
        }

        Future<Optional<Vet>> future = vetAvailabilityExecutor.submit(() -> performVetAvailabilityCheck(scheduledStart));

        try {
            return future.get(vetCheckTimeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            logger.warn("Vet availability check timed out after {}ms, proceeding without vet assignment", vetCheckTimeoutMs);
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Vet availability check interrupted, proceeding without vet assignment");
            return Optional.empty();
        } catch (ExecutionException e) {
            logger.warn("Vet availability check failed, proceeding without vet assignment", e.getCause());
            return Optional.empty();
        }
    }

    private Optional<Vet> performVetAvailabilityCheck(Instant scheduledStart) {
        List<Vet> allVets = vetRepository.findAll();

        for (Vet vet : allVets) {
            boolean isAvailable = !bookingRepository.existsByVetIdAndScheduledStartAndActiveTrue(
                vet.getId(),
                scheduledStart
            );

            if (isAvailable) {
                return Optional.of(vet);
            }
        }

        return Optional.empty();
    }

    private String generateBookingReference() {
        return "TC-" + Instant.now().getEpochSecond() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private EmergencyTeleConsultationConfirmation mapToConfirmation(TeleConsultationBooking booking) {
        return new EmergencyTeleConsultationConfirmation(
            booking.getBookingReference(),
            booking.getStatus(),
            booking.getTriageLevel(),
            booking.getScheduledStart(),
            booking.getVet() != null ? booking.getVet().getId() : null,
            booking.getPet().getId()
        );
    }
}