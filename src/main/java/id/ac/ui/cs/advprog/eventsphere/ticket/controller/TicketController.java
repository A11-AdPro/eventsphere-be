package id.ac.ui.cs.advprog.eventsphere.ticket.controller;

import id.ac.ui.cs.advprog.eventsphere.ticket.dto.TicketRequest;
import id.ac.ui.cs.advprog.eventsphere.ticket.dto.TicketResponse;
import id.ac.ui.cs.advprog.eventsphere.ticket.service.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService service;

    public TicketController(TicketService service) {
        this.service = service;
    }

    @PreAuthorize("hasRole('ROLE_ATTENDEE') or hasRole('ROLE_ORGANIZER') or hasRole('ROLE_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable Long id) {
        try {
            TicketResponse response = service.getTicketById(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PreAuthorize("hasRole('ROLE_ORGANIZER')")
    @PostMapping
    public ResponseEntity<TicketResponse> create(@RequestBody TicketRequest request) {
        try {
            TicketResponse response = service.addTicket(request).join();  // blocking tunggu hasil
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }


    @PreAuthorize("hasRole('ROLE_ORGANIZER')")
    @PutMapping("/{id}")
    public ResponseEntity<TicketResponse> update(@PathVariable Long id, @RequestBody TicketRequest request) {
        try {
            TicketResponse response = service.updateTicket(id, request).join();  // tunggu hasilnya
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }


    @PreAuthorize("hasRole('ROLE_ATTENDEE') or hasRole('ROLE_ORGANIZER') or hasRole('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<List<TicketResponse>> list() {
        List<TicketResponse> tickets = service.getAvailableTickets().join();  // <-- pakai join() biar blocking
        return ResponseEntity.ok(tickets);
    }

    /*
    // Bisa tambahkan pembelian tiket (opsional, misal role Attendee juga boleh beli)
    @PreAuthorize("hasRole('ROLE_ATTENDEE')")
    @PostMapping("/{id}/purchase")
    public ResponseEntity<TicketResponse> purchase(@PathVariable Long id) {
        try {
            TicketResponse response = service.purchaseTicket(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

     */

    // Admin dapat hapus tiket (D)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            service.deleteTicket(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
