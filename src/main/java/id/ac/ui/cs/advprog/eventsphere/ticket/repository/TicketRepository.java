package id.ac.ui.cs.advprog.eventsphere.ticket.repository;

import id.ac.ui.cs.advprog.eventsphere.ticket.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    // Kalau perlu, bisa tambahkan query method khusus di sini
}

