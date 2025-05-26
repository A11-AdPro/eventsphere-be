package id.ac.ui.cs.advprog.eventsphere.ticket.repository;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.repository.UserRepository;
import id.ac.ui.cs.advprog.eventsphere.event.model.Event;
import id.ac.ui.cs.advprog.eventsphere.event.repository.EventRepository;
import id.ac.ui.cs.advprog.eventsphere.ticket.model.Ticket;
import id.ac.ui.cs.advprog.eventsphere.ticket.model.TicketCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class TicketRepositoryTest {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Save and find a ticket by id")
    public void testSaveAndFindTicket() {
        // 1. Buat dan simpan user dummy
        User organizer = new User();
        organizer.setFullName("dummyorganizer");
        organizer.setEmail("organizer@example.com");
        organizer.setPassword("securepassword"); // kalau ada validasi
        userRepository.save(organizer);

        // 2. Buat dan simpan event dummy
        Event event = new Event();
        event.setTitle("Dummy Event");
        event.setDescription("Deskripsi singkat");
        event.setEventDate(LocalDateTime.now().plusDays(3));
        event.setLocation("Jakarta");
        event.setPrice(BigDecimal.valueOf(50000));
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());
        event.setOrganizer(organizer);
        event.setActive(true);
        event.setCancelled(false);
        event = eventRepository.save(event);

        // 3. Buat dan simpan ticket dummy
        Ticket ticket = new Ticket();
        ticket.setName("Test Ticket");
        ticket.setCategory(TicketCategory.VIP);
        ticket.setPrice(100);
        ticket.setQuota(50);
        ticket.setSold(0);
        ticket.setEvent(event); // relasi many-to-one

        Ticket savedTicket = ticketRepository.save(ticket);

        // 4. Validasi penyimpanan
        assertThat(savedTicket).isNotNull();
        assertThat(savedTicket.getId()).isNotNull();

        Ticket foundTicket = ticketRepository.findById(savedTicket.getId()).orElse(null);

        assertThat(foundTicket).isNotNull();
        assertThat(foundTicket.getName()).isEqualTo("Test Ticket");
        assertThat(foundTicket.getCategory()).isEqualTo(TicketCategory.VIP);
        assertThat(foundTicket.getPrice()).isEqualTo(100);
        assertThat(foundTicket.getQuota()).isEqualTo(50);
        assertThat(foundTicket.getSold()).isEqualTo(0);
        assertThat(foundTicket.getEvent().getId()).isEqualTo(event.getId());
    }
}
