-- Add transfer_status column
ALTER TABLE scheduled_operations
ADD COLUMN transfer_status VARCHAR(50);

-- Add transferred_to column
ALTER TABLE scheduled_operations
ADD COLUMN transferred_to VARCHAR(50);