-- Add to existing schema file

CREATE TABLE IF NOT EXISTS emergency_consultations (
  id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  pet_id INT UNSIGNED NOT NULL,
  vet_id INT UNSIGNED NOT NULL,
  scheduled_time TIMESTAMP(6) NOT NULL,
  severity VARCHAR(20) NOT NULL,
  priority VARCHAR(20) NOT NULL,
  symptoms VARCHAR(2000) NOT NULL,
  confirmed_at TIMESTAMP(6) NOT NULL,
  CONSTRAINT fk_emergency_consultation_pet FOREIGN KEY (pet_id) REFERENCES pets(id) ON DELETE CASCADE,
  CONSTRAINT fk_emergency_consultation_vet FOREIGN KEY (vet_id) REFERENCES vets(id) ON DELETE CASCADE,
  CONSTRAINT uk_vet_scheduled_time UNIQUE (vet_id, scheduled_time),
  INDEX idx_pet_id (pet_id),
  INDEX idx_vet_id (vet_id),
  INDEX idx_scheduled_time (scheduled_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
