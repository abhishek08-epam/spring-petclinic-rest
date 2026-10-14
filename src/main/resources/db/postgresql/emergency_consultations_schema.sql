-- Add to existing schema file

CREATE TABLE IF NOT EXISTS emergency_consultations (
  id SERIAL PRIMARY KEY,
  pet_id INTEGER NOT NULL,
  vet_id INTEGER NOT NULL,
  scheduled_time TIMESTAMP(6) NOT NULL,
  severity VARCHAR(20) NOT NULL,
  priority VARCHAR(20) NOT NULL,
  symptoms VARCHAR(2000) NOT NULL,
  confirmed_at TIMESTAMP(6) NOT NULL,
  CONSTRAINT fk_emergency_consultation_pet FOREIGN KEY (pet_id) REFERENCES pets(id) ON DELETE CASCADE,
  CONSTRAINT fk_emergency_consultation_vet FOREIGN KEY (vet_id) REFERENCES vets(id) ON DELETE CASCADE,
  CONSTRAINT uk_vet_scheduled_time UNIQUE (vet_id, scheduled_time)
);

CREATE INDEX idx_emergency_consultations_pet_id ON emergency_consultations(pet_id);
CREATE INDEX idx_emergency_consultations_vet_id ON emergency_consultations(vet_id);
CREATE INDEX idx_emergency_consultations_scheduled_time ON emergency_consultations(scheduled_time);
