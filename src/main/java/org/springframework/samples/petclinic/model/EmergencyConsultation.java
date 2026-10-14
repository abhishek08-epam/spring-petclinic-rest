package org.springframework.samples.petclinic.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

@Entity
@Table(
    name = "emergency_consultations",
    uniqueConstraints = @UniqueConstraint(columnNames = {"vet_id", "scheduled_time"})
)
public class EmergencyConsultation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    @NotNull
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vet_id", nullable = false)
    @NotNull
    private Vet vet;

    @Column(name = "scheduled_time", nullable = false)
    @NotNull
    private Instant scheduledTime;

    @Column(name = "severity", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull
    private SymptomSeverity severity;

    @Column(name = "priority", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull
    private TriagePriority priority;

    @Column(name = "symptoms", nullable = false, length = 2000)
    @NotBlank
    @Size(max = 2000)
    private String symptoms;

    @Column(name = "confirmed_at", nullable = false)
    @NotNull
    private Instant confirmedAt;

    public EmergencyConsultation() {
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

    public Instant getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(Instant scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public SymptomSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(SymptomSeverity severity) {
        this.severity = severity;
    }

    public TriagePriority getPriority() {
        return priority;
    }

    public void setPriority(TriagePriority priority) {
        this.priority = priority;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public Instant getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(Instant confirmedAt) {
        this.confirmedAt = confirmedAt;
    }
}
