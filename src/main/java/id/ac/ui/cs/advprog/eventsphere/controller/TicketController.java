package id.ac.ui.cs.advprog.eventsphere.controller;

import id.ac.ui.cs.advprog.eventsphere.dto.*;
import id.ac.ui.cs.advprog.eventsphere.service.TicketService;
import id.ac.ui.cs.advprog.eventsphere.service.TicketServiceImpl;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService service;

    // ✅ constructor injection
    public TicketController(TicketService service) {
        this.service = service;
    }

    @PostMapping
    public TicketResponse create(@RequestBody TicketRequest request) {
        return service.addTicket(request);
    }

    @PutMapping("/{id}")
    public TicketResponse update(@PathVariable Long id, @RequestBody TicketRequest request) {
        return service.updateTicket(id, request);
    }

    @GetMapping
    public List<TicketResponse> list() {
        return service.getAvailableTickets();
    }

    @PostMapping("/{id}/purchase")
    public TicketResponse purchase(@PathVariable Long id) {
        return service.purchaseTicket(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.deleteTicket(id);
    }
}
