-- Add this to the existing schema.sql file

-- New table for emergency tele-consultations
CREATE TABLE IF NOT EXISTS tele_consultations (
  id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  pet_id INT UNSIGNED NOT NULL,
  vet_id INT UNSIGNED NOT NULL,
  start_at TIMESTAMP NOT NULL,
  end_at TIMESTAMP NOT NULL,
  status VARCHAR(20) NOT NULL,
  emergency BOOLEAN NOT NULL DEFAULT TRUE,
  reason VARCHAR(255) NOT NULL,
  severity VARCHAR(20) NOT NULL,
  triage_priority VARCHAR(20) NOT NULL,
  meeting_url VARCHAR(512),
  version INT UNSIGNED NOT NULL DEFAULT 0,
  INDEX ix_tc_vet_start (vet_id, start_at),
  INDEX ix_tc_pet_start (pet_id, start_at),
  INDEX ix_tc_vet_status_start (vet_id, status, start_at),
  CONSTRAINT fk_tc_pet FOREIGN KEY (pet_id) REFERENCES pets(id),
  CONSTRAINT fk_tc_vet FOREIGN KEY (vet_id) REFERENCES vets(id),
  CONSTRAINT chk_tc_time CHECK (end_at > start_at)
) engine=InnoDB;