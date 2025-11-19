-- Add source_version column to wearable_devices table
-- This stores the Apple HealthKit source version metadata
-- Moved from MongoDB metadata.sourceVersion for normalization

ALTER TABLE wearable_devices ADD COLUMN source_version VARCHAR(255);

-- Create index for querying by device source version
CREATE INDEX idx_wearable_devices_source_version ON wearable_devices(source_version);
