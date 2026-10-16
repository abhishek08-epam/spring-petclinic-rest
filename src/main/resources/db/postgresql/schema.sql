-- Add this to the existing schema.sql file

-- New table for emergency tele-consultations
CREATE TABLE IF NOT EXISTS tele_consultations (
  id SERIAL PRIMARY KEY,
  pet_id INTEGER NOT NULL,
  vet_id INTEGER NOT NULL,
  start_at TIMESTAMP NOT NULL,
  end_at TIMESTAMP NOT NULL,
  status VARCHAR(20) NOT NULL,
  emergency BOOLEAN NOT NULL DEFAULT TRUE,
  reason VARCHAR(255) NOT NULL,
  severity VARCHAR(20) NOT NULL,
  triage_priority VARCHAR(20) NOT NULL,
  meeting_url VARCHAR(512),
  version INTEGER NOT NULL DEFAULT 0,
  CONSTRAINT fk_tc_pet FOREIGN KEY (pet_id) REFERENCES pets(id),
  CONSTRAINT fk_tc_vet FOREIGN KEY (vet_id) REFERENCES vets(id),
  CONSTRAINT chk_tc_time CHECK (end_at > start_at)
);

CREATE INDEX ix_tc_vet_start ON tele_consultations(vet_id, start_at);
CREATE INDEX ix_tc_pet_start ON tele_consultations(pet_id, start_at);
CREATE INDEX ix_tc_vet_status_start ON tele_consultations(vet_id, status, start_at);

-- Exclusion constraint to prevent overbooking (PostgreSQL specific)
CREATE EXTENSION IF NOT EXISTS btree_gist;
ALTER TABLE tele_consultations ADD CONSTRAINT no_overlap_vet_time 
  EXCLUDE USING gist (vet_id WITH =, tsrange(start_at, end_at) WITH &&) 
  WHERE (status IN ('REQUESTED', 'CONFIRMED'));