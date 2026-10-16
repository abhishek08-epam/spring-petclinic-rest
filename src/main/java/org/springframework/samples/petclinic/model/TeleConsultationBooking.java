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
package org.springframework.samples.petclinic.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * Emergency tele-consultation booking for pets.
 *
 * @author PetClinic Team
 */
@Entity
@Table(name = "tele_consultations")
public class TeleConsultationBooking extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "pet_id", nullable = false)
	@NotNull
	private Pet pet;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vet_id", nullable = false)
	@NotNull
	private Vet vet;

	@Column(name = "start_at", nullable = false)
	@NotNull
	private Instant startAt;

	@Column(name = "end_at", nullable = false)
	@NotNull
	private Instant endAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	@NotNull
	private BookingStatus status;

	@Column(name = "emergency", nullable = false)
	private boolean emergency = true;

	@Column(name = "reason", nullable = false)
	@NotBlank
	private String reason;

	@Enumerated(EnumType.STRING)
	@Column(name = "severity", nullable = false)
	@NotNull
	private Severity severity;

	@Enumerated(EnumType.STRING)
	@Column(name = "triage_priority", nullable = false)
	@NotNull
	private TriagePriority triagePriority;

	@Column(name = "meeting_url")
	private String meetingUrl;

	@Version
	@Column(name = "version", nullable = false)
	private Integer version = 0;

	public TeleConsultationBooking() {
	}

	public Pet getPet() {
		return pet;
	}

	public void setPet(Pet pet) {
		this.pet = pet;
	}

	public Vet getVet() {
		return vet;
	}

	public void setVet(Vet vet) {
		this.vet = vet;
	}

	public Instant getStartAt() {
		return startAt;
	}

	public void setStartAt(Instant startAt) {
		this.startAt = startAt;
	}

	public Instant getEndAt() {
		return endAt;
	}

	public void setEndAt(Instant endAt) {
		this.endAt = endAt;
	}

	public BookingStatus getStatus() {
		return status;
	}

	public void setStatus(BookingStatus status) {
		this.status = status;
	}

	public boolean isEmergency() {
		return emergency;
	}

	public void setEmergency(boolean emergency) {
		this.emergency = emergency;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public Severity getSeverity() {
		return severity;
	}

	public void setSeverity(Severity severity) {
		this.severity = severity;
	}

	public TriagePriority getTriagePriority() {
		return triagePriority;
	}

	public void setTriagePriority(TriagePriority triagePriority) {
		this.triagePriority = triagePriority;
	}

	public String getMeetingUrl() {
		return meetingUrl;
	}

	public void setMeetingUrl(String meetingUrl) {
		this.meetingUrl = meetingUrl;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public enum BookingStatus {
		REQUESTED, CONFIRMED, CANCELLED, COMPLETED
	}

	public enum Severity {
		MILD, MODERATE, SEVERE
	}

	public enum TriagePriority {
		LOW, MEDIUM, HIGH
	}

}
