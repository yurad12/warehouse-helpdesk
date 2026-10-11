package com.skala.warehouse_helpdesk.repository;

import java.time.Instant;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.skala.warehouse_helpdesk.domain.Ticket;

@Repository
public class TicketRepository {

    private static final RowMapper<Ticket> TICKET_ROW_MAPPER = (resultSet, rowNumber) -> {
        Timestamp resolvedAt = resultSet.getTimestamp("resolved_at");
        return new Ticket(
                resultSet.getString("ticket_no"),
                resultSet.getString("pallet_id"),
                resultSet.getString("warehouse_id"),
                resultSet.getString("reason"),
                resultSet.getString("status"),
                resultSet.getString("resolution_summary"),
                resultSet.getString("action_taken"),
                resultSet.getString("resolved_by"),
                resolvedAt == null ? null : resolvedAt.toInstant(),
                resultSet.getBoolean("reusable"));
    };

    private final JdbcTemplate jdbcTemplate;

    public TicketRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Ticket save(
            String palletId,
            String warehouseId,
            String reason) {
        Long sequence = jdbcTemplate.queryForObject("SELECT nextval('ticket_number_seq')", Long.class);
        String ticketNo = "TK-" + sequence;

        Ticket ticket = new Ticket(
                ticketNo,
                palletId,
                warehouseId,
                reason,
                "PENDING",
                null,
                null,
                null,
                null,
                false);

        try {
            jdbcTemplate.update(
                    """
                    INSERT INTO tickets (ticket_no, pallet_id, warehouse_id, reason, status, reusable)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """,
                    ticket.ticketNo(),
                    ticket.palletId(),
                    ticket.warehouseId(),
                    ticket.reason(),
                    ticket.status(),
                    ticket.reusable());
            return ticket;
        }
        catch (DuplicateKeyException exception) {
            return findPendingTicket(palletId, warehouseId, reason).orElseThrow(() -> exception);
        }
    }

    public Optional<Ticket> findPendingTicket(
            String palletId,
            String warehouseId,
            String reason) {
        List<Ticket> tickets = jdbcTemplate.query(
                """
                SELECT * FROM tickets
                WHERE pallet_id = ? AND warehouse_id = ? AND reason = ? AND status = 'PENDING'
                """,
                TICKET_ROW_MAPPER,
                palletId,
                warehouseId,
                reason);
        return tickets.stream().findFirst();
    }

    public Ticket findByTicketNo(String ticketNo) {
        return jdbcTemplate.query(
                "SELECT * FROM tickets WHERE ticket_no = ?",
                TICKET_ROW_MAPPER,
                ticketNo)
                .stream()
                .findFirst()
                .orElse(null);
    }

    public List<Ticket> findReusableResolvedTickets(String warehouseId) {
        return jdbcTemplate.query(
                """
                SELECT * FROM tickets
                WHERE warehouse_id = ? AND status = 'RESOLVED' AND reusable = TRUE
                ORDER BY resolved_at
                """,
                TICKET_ROW_MAPPER,
                warehouseId);
    }

    public Ticket resolve(
            Ticket ticket,
            String resolutionSummary,
            String actionTaken,
            String resolvedBy,
            Instant resolvedAt,
            boolean reusable) {
        int updatedRows = jdbcTemplate.update(
                """
                UPDATE tickets
                SET status = 'RESOLVED',
                    resolution_summary = ?,
                    action_taken = ?,
                    resolved_by = ?,
                    resolved_at = ?,
                    reusable = ?
                WHERE ticket_no = ? AND status = 'PENDING'
                """,
                resolutionSummary,
                actionTaken,
                resolvedBy,
                Timestamp.from(resolvedAt),
                reusable,
                ticket.ticketNo());
        if (updatedRows == 0) {
            throw new IllegalStateException("이미 처리 완료된 티켓입니다.");
        }
        return findByTicketNo(ticket.ticketNo());
    }
}
