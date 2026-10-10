CREATE SEQUENCE IF NOT EXISTS ticket_number_seq START WITH 1001;

CREATE TABLE IF NOT EXISTS tickets (
    ticket_no VARCHAR(30) PRIMARY KEY,
    pallet_id VARCHAR(100) NOT NULL,
    warehouse_id VARCHAR(100) NOT NULL,
    reason TEXT NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'RESOLVED')),
    resolution_summary TEXT,
    action_taken TEXT,
    resolved_by VARCHAR(100),
    resolved_at TIMESTAMPTZ,
    reusable BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_tickets_pending_request
    ON tickets (warehouse_id, pallet_id, reason)
    WHERE status = 'PENDING';
