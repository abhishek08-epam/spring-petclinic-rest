package org.springframework.samples.petclinic.teleconsultation;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.samples.petclinic.model.BaseEntity;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.vet.Vet;

import java.time.Instant;

@Entity
@Table(
    name = "tele_consultation_bookings",
    indexes = {
        @Index(name = "idx_vet_scheduled", columnList = "vet_id,scheduled_start"),
        @Index(name = "idx_pet_scheduled", columnList = "pet_id,scheduled_start"),
        @Index(name = "idx_status_scheduled", columnList = "status,scheduled_start")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_vet_slot_active", columnNames = {"vet_id", "scheduled_start", "active"})
    }
)
public class TeleConsultationBooking extends BaseEntity {

    public enum Severity {
        MILD, MODERATE, SEVERE
    }

    public enum TriageLevel {
        GREEN, YELLOW, RED
    }

    public enum BookingStatus {
        REQUESTED, TRIAGED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED, NO_SHOW
    }

    public enum Channel {
        VIDEO
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    @NotNull
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vet_id")
    private Vet vet;

    @Column(name = "requested_at", nullable = false)
    @NotNull
    private Instant requestedAt;

    @Column(name = "scheduled_start")
    private Instant scheduledStart;

    @Column(name = "scheduled_end")
    private Instant scheduledEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull
    private BookingStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "triage_level", nullable = false, length = 20)
    @NotNull
    private TriageLevel triageLevel;

    @Column(nullable = false, length = 1000)
    @NotBlank
    @Size(max = 1000)
    private String symptoms;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull
    private Channel channel;

    @Column(name = "meeting_url", length = 500)
    @Size(max = 500)
    private String meetingUrl;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "booking_reference", unique = true, nullable = false, length = 50)
    @NotBlank
    @Size(max = 50)
    private String bookingReference;

    @Version
    private Long version;

    public TeleConsultationBooking() {
        this.active = true;
        this.channel = Channel.VIDEO;
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

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(Instant requestedAt) {
        this.requestedAt = requestedAt;
    }

    public Instant getScheduledStart() {
        return scheduledStart;
    }

    public void setScheduledStart(Instant scheduledStart) {
        this.scheduledStart = scheduledStart;
    }

    public Instant getScheduledEnd() {
        return scheduledEnd;
    }

    public void setScheduledEnd(Instant scheduledEnd) {
        this.scheduledEnd = scheduledEnd;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public TriageLevel getTriageLevel() {
        return triageLevel;
    }

    public void setTriageLevel(TriageLevel triageLevel) {
        this.triageLevel = triageLevel;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getMeetingUrl() {
        return meetingUrl;
    }

    public void setMeetingUrl(String meetingUrl) {
        this.meetingUrl = meetingUrl;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}