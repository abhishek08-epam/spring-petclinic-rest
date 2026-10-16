CREATE TABLE tele_consultation_bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pet_id INT NOT NULL,
    vet_id INT,
    requested_at TIMESTAMP NOT NULL,
    scheduled_start TIMESTAMP,
    scheduled_end TIMESTAMP,
    status VARCHAR(20) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    triage_level VARCHAR(20) NOT NULL,
    symptoms VARCHAR(1000) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    meeting_url VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    booking_reference VARCHAR(50) NOT NULL UNIQUE,
    version BIGINT,
    CONSTRAINT fk_tele_consultation_pet FOREIGN KEY (pet_id) REFERENCES pets(id),
    CONSTRAINT fk_tele_consultation_vet FOREIGN KEY (vet_id) REFERENCES vets(id),
    CONSTRAINT uk_vet_slot_active UNIQUE (vet_id, scheduled_start, active)
);

CREATE INDEX idx_vet_scheduled ON tele_consultation_bookings(vet_id, scheduled_start);
CREATE INDEX idx_pet_scheduled ON tele_consultation_bookings(pet_id, scheduled_start);
CREATE INDEX idx_status_scheduled ON tele_consultation_bookings(status, scheduled_start);