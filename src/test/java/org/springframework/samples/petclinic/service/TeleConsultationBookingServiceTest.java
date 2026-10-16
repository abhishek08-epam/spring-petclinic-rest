package org.springframework.samples.petclinic.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.TeleConsultationBooking;
import org.springframework.samples.petclinic.model.TeleConsultationBooking.Severity;
import org.springframework.samples.petclinic.model.TeleConsultationBooking.TriagePriority;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.repository.PetRepository;
import org.springframework.samples.petclinic.repository.TeleConsultationBookingRepository;
import org.springframework.samples.petclinic.repository.VetRepository;
import org.springframework.samples.petclinic.rest.advice.ResourceNotFoundException;
import org.springframework.samples.petclinic.rest.dto.EmergencyTeleConsultationConfirmationDto;
import org.springframework.samples.petclinic.rest.dto.EmergencyTeleConsultationRequestDto;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TeleConsultationBookingServiceTest {

	TeleConsultationBookingRepository bookingRepository = mock(TeleConsultationBookingRepository.class);
	PetRepository petRepository = mock(PetRepository.class);
	VetRepository vetRepository = mock(VetRepository.class);

	TeleConsultationBookingService service = new TeleConsultationBookingService(
		bookingRepository, petRepository, vetRepository
	);

	@Test
	@DisplayName("AC-4: returns confirmation with booking details (bookingId, scheduled time, triage priority)")
	void createEmergencyBooking_success_shouldReturnConfirmationDto() {
		Pet pet = new Pet();
		pet.setId(1);

		Vet vet = new Vet();
		vet.setId(3);

		Instant preferredStartAt = Instant.parse("2026-01-01T10:00:00Z");

		EmergencyTeleConsultationRequestDto request = new EmergencyTeleConsultationRequestDto(
			1,
			Severity.SEVERE,
			"Breathing issues",
			preferredStartAt,
			15
		);

		when(petRepository.findById(1)).thenReturn(Optional.of(pet));
		when(vetRepository.findAll()).thenReturn(List.of(vet));
		when(bookingRepository.findOverlappingBookings(eq(3), any(), any())).thenReturn(List.of());

		when(bookingRepository.save(any())).thenAnswer(invocation -> {
			TeleConsultationBooking b = invocation.getArgument(0);
			b.setId(42);
			return b;
		});

		EmergencyTeleConsultationConfirmationDto confirmation = service.createEmergencyBooking(request);

		assertThat(confirmation.bookingId()).isEqualTo(42);
		assertThat(confirmation.petId()).isEqualTo(1);
		assertThat(confirmation.vetId()).isEqualTo(3);
		assertThat(confirmation.startAt()).isEqualTo(preferredStartAt);
		assertThat(confirmation.endAt()).isAfter(preferredStartAt);
		assertThat(confirmation.triagePriority()).isEqualTo(TriagePriority.HIGH);
		assertThat(confirmation.status()).isEqualTo("CONFIRMED");
		assertThat(confirmation.meetingUrl()).isNotBlank();
	}

	@ParameterizedTest(name = "AC-2: severity {0} maps deterministically to triage")
	@EnumSource(Severity.class)
	void createEmergencyBooking_triagePriorityMapping_shouldBeDeterministic(Severity severity) {
		Pet pet = new Pet();
		pet.setId(1);

		Vet vet = new Vet();
		vet.setId(3);

		EmergencyTeleConsultationRequestDto request = new EmergencyTeleConsultationRequestDto(
			1,
			severity,
			"Symptoms",
			Instant.parse("2026-01-01T10:00:00Z"),
			15
		);

		when(petRepository.findById(1)).thenReturn(Optional.of(pet));
		when(vetRepository.findAll()).thenReturn(List.of(vet));
		when(bookingRepository.findOverlappingBookings(eq(3), any(), any())).thenReturn(List.of());

		when(bookingRepository.save(any())).thenAnswer(invocation -> {
			TeleConsultationBooking b = invocation.getArgument(0);
			b.setId(100 + severity.ordinal());
			return b;
		});

		EmergencyTeleConsultationConfirmationDto confirmation = service.createEmergencyBooking(request);

		TriagePriority expected = switch (severity) {
			case MILD -> TriagePriority.LOW;
			case MODERATE -> TriagePriority.MEDIUM;
			case SEVERE -> TriagePriority.HIGH;
		};

		assertThat(confirmation.triagePriority()).isEqualTo(expected);
	}

	@Test
	@DisplayName("BC-1: missing severity defaults to MILD and produces LOW triage")
	void createEmergencyBooking_missingSeverity_shouldDefaultToLowPriority() {
		Pet pet = new Pet();
		pet.setId(1);

		Vet vet = new Vet();
		vet.setId(3);

		EmergencyTeleConsultationRequestDto request = new EmergencyTeleConsultationRequestDto(
			1,
			null,
			"Symptoms",
			Instant.parse("2026-01-01T10:00:00Z"),
			15
		);

		when(petRepository.findById(1)).thenReturn(Optional.of(pet));
		when(vetRepository.findAll()).thenReturn(List.of(vet));
		when(bookingRepository.findOverlappingBookings(eq(3), any(), any())).thenReturn(List.of());
		when(bookingRepository.save(any())).thenAnswer(invocation -> {
			TeleConsultationBooking b = invocation.getArgument(0);
			b.setId(42);
			return b;
		});

		EmergencyTeleConsultationConfirmationDto confirmation = service.createEmergencyBooking(request);

		assertThat(confirmation.triagePriority()).isEqualTo(TriagePriority.LOW);
	}

	@Test
	@DisplayName("BC-2: vet availability check failure still books (best effort)")
	void createEmergencyBooking_availabilityCheckFailure_shouldStillBook() {
		Pet pet = new Pet();
		pet.setId(1);

		Vet vet = new Vet();
		vet.setId(3);

		EmergencyTeleConsultationRequestDto request = new EmergencyTeleConsultationRequestDto(
			1,
			Severity.MODERATE,
			"Symptoms",
			Instant.parse("2026-01-01T10:00:00Z"),
			15
		);

		when(petRepository.findById(1)).thenReturn(Optional.of(pet));
		when(vetRepository.findAll()).thenReturn(List.of(vet));
		when(bookingRepository.findOverlappingBookings(eq(3), any(), any()))
			.thenThrow(new RuntimeException("DB read failed"));

		when(bookingRepository.save(any())).thenAnswer(invocation -> {
			TeleConsultationBooking b = invocation.getArgument(0);
			b.setId(99);
			return b;
		});

		EmergencyTeleConsultationConfirmationDto confirmation = service.createEmergencyBooking(request);

		assertThat(confirmation.bookingId()).isEqualTo(99);
		assertThat(confirmation.vetId()).isEqualTo(3);
		assertThat(confirmation.triagePriority()).isEqualTo(TriagePriority.MEDIUM);
	}

	@Test
	@DisplayName("BC-2: vet availability check times out still books (best effort)")
	void createEmergencyBooking_availabilityTimeout_shouldStillBook() throws Exception {
		// This test forces the internal Future#get(timeout) path by replacing the executorService
		// with one that never completes.
		replaceExecutorServiceWithNeverCompletingTask();

		Pet pet = new Pet();
		pet.setId(1);

		Vet vet = new Vet();
		vet.setId(3);

		EmergencyTeleConsultationRequestDto request = new EmergencyTeleConsultationRequestDto(
			1,
			Severity.MODERATE,
			"Symptoms",
			Instant.parse("2026-01-01T10:00:00Z"),
			15
		);

		when(petRepository.findById(1)).thenReturn(Optional.of(pet));
		when(vetRepository.findAll()).thenReturn(List.of(vet));

		// bookingRepository.save still called even though availability never returns
		when(bookingRepository.save(any())).thenAnswer(invocation -> {
			TeleConsultationBooking b = invocation.getArgument(0);
			b.setId(101);
			return b;
		});

		EmergencyTeleConsultationConfirmationDto confirmation = service.createEmergencyBooking(request);
		assertThat(confirmation.bookingId()).isEqualTo(101);
		assertThat(confirmation.vetId()).isEqualTo(3);
	}

	@Test
	@DisplayName("BC-4: multiple requests for same pet processed independently (no dedup)")
	void createEmergencyBooking_multipleRequests_shouldCreateMultipleBookings() {
		Pet pet = new Pet();
		pet.setId(1);

		Vet vet = new Vet();
		vet.setId(3);

		when(petRepository.findById(1)).thenReturn(Optional.of(pet));
		when(vetRepository.findAll()).thenReturn(List.of(vet));
		when(bookingRepository.findOverlappingBookings(eq(3), any(), any())).thenReturn(List.of());
		when(bookingRepository.save(any())).thenAnswer(new org.mockito.stubbing.Answer<TeleConsultationBooking>() {
			int id = 1;

			@Override
			public TeleConsultationBooking answer(org.mockito.invocation.InvocationOnMock invocation) {
				TeleConsultationBooking b = invocation.getArgument(0);
				b.setId(id++);
				return b;
			}
		});

		EmergencyTeleConsultationRequestDto request1 = new EmergencyTeleConsultationRequestDto(
			1, Severity.MILD, "Symptoms1", Instant.parse("2026-01-01T10:00:00Z"), 15
		);
		EmergencyTeleConsultationRequestDto request2 = new EmergencyTeleConsultationRequestDto(
			1, Severity.MILD, "Symptoms2", Instant.parse("2026-01-01T10:05:00Z"), 15
		);

		EmergencyTeleConsultationConfirmationDto c1 = service.createEmergencyBooking(request1);
		EmergencyTeleConsultationConfirmationDto c2 = service.createEmergencyBooking(request2);

		assertThat(c1.bookingId()).isNotEqualTo(c2.bookingId());
		verify(bookingRepository, times(2)).save(any(TeleConsultationBooking.class));

		ArgumentCaptor<TeleConsultationBooking> captor = ArgumentCaptor.forClass(TeleConsultationBooking.class);
		verify(bookingRepository, times(2)).save(captor.capture());
		assertThat(captor.getAllValues()).hasSize(2);
		assertThat(captor.getAllValues().get(0).getReason()).isEqualTo("Symptoms1");
		assertThat(captor.getAllValues().get(1).getReason()).isEqualTo("Symptoms2");
	}

	@Test
	@DisplayName("Not-found: when pet does not exist -> ResourceNotFoundException")
	void createEmergencyBooking_petNotFound_shouldThrow() {
		EmergencyTeleConsultationRequestDto request = new EmergencyTeleConsultationRequestDto(
			999,
			Severity.MILD,
			"Symptoms",
			Instant.parse("2026-01-01T10:00:00Z"),
			15
		);

		when(petRepository.findById(999)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.createEmergencyBooking(request))
			.isInstanceOf(ResourceNotFoundException.class);

		verify(bookingRepository, never()).save(any());
	}

	@Test
	@DisplayName("Not-found: when no vets exist -> ResourceNotFoundException")
	void createEmergencyBooking_noVets_shouldThrow() {
		Pet pet = new Pet();
		pet.setId(1);

		EmergencyTeleConsultationRequestDto request = new EmergencyTeleConsultationRequestDto(
			1,
			Severity.MILD,
			"Symptoms",
			Instant.parse("2026-01-01T10:00:00Z"),
			15
		);

		when(petRepository.findById(1)).thenReturn(Optional.of(pet));
		when(vetRepository.findAll()).thenReturn(List.of());

		assertThatThrownBy(() -> service.createEmergencyBooking(request))
			.isInstanceOf(ResourceNotFoundException.class);

		verify(bookingRepository, never()).save(any());
	}

	private void replaceExecutorServiceWithNeverCompletingTask() throws Exception {
		var neverCompletingExecutor = new java.util.concurrent.ExecutorService() {
			@Override
			public void shutdown() {
			}

			@Override
			public List<Runnable> shutdownNow() {
				return List.of();
			}

			@Override
			public boolean isShutdown() {
				return false;
			}

			@Override
			public boolean isTerminated() {
				return false;
			}

			@Override
			public boolean awaitTermination(long timeout, java.util.concurrent.TimeUnit unit) {
				return false;
			}

			@Override
			public <T> Future<T> submit(java.util.concurrent.Callable<T> task) {
				return new Future<>() {
					@Override
					public boolean cancel(boolean mayInterruptIfRunning) {
						return true;
					}

					@Override
					public boolean isCancelled() {
						return false;
					}

					@Override
					public boolean isDone() {
						return false;
					}

					@Override
					public T get() throws InterruptedException {
						Thread.sleep(10_000);
						return null;
					}

					@Override
					public T get(long timeout, java.util.concurrent.TimeUnit unit) throws java.util.concurrent.TimeoutException {
						throw new java.util.concurrent.TimeoutException("forced timeout");
					}
				};
			}

			@Override
			public <T> Future<T> submit(Runnable task, T result) {
				throw new UnsupportedOperationException();
			}

			@Override
			public Future<?> submit(Runnable task) {
				throw new UnsupportedOperationException();
			}

			@Override
			public <T> List<Future<T>> invokeAll(java.util.Collection<? extends java.util.concurrent.Callable<T>> tasks) {
				throw new UnsupportedOperationException();
			}

			@Override
			public <T> List<Future<T>> invokeAll(java.util.Collection<? extends java.util.concurrent.Callable<T>> tasks,
												 long timeout,
												 java.util.concurrent.TimeUnit unit) {
				throw new UnsupportedOperationException();
			}

			@Override
			public <T> T invokeAny(java.util.Collection<? extends java.util.concurrent.Callable<T>> tasks) {
				throw new UnsupportedOperationException();
			}

			@Override
			public <T> T invokeAny(java.util.Collection<? extends java.util.concurrent.Callable<T>> tasks, long timeout,
								 java.util.concurrent.TimeUnit unit) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void execute(Runnable command) {
			}
		};

		Field executorServiceField = TeleConsultationBookingService.class.getDeclaredField("executorService");
		executorServiceField.setAccessible(true);
		executorServiceField.set(service, neverCompletingExecutor);
	}
}
