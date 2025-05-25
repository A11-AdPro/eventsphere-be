package id.ac.ui.cs.advprog.eventsphere.ticket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.Role;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.repository.UserRepository;
import id.ac.ui.cs.advprog.eventsphere.ticket.model.TicketCategory;
import id.ac.ui.cs.advprog.eventsphere.ticket.dto.TicketRequest;
import id.ac.ui.cs.advprog.eventsphere.ticket.dto.TicketResponse;
import id.ac.ui.cs.advprog.eventsphere.ticket.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;


import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(TicketController.class)
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TicketService ticketService;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User organizer;

    @BeforeEach
    void setUp() {
        organizer = new User();
        organizer.setId(1L);
        organizer.setEmail("organizer@example.com");
        organizer.setRole(Role.ORGANIZER);

        when(userRepository.findByEmail("organizer@example.com")).thenReturn(Optional.of(organizer));
    }

    @Test
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void createTicket_shouldReturnCreated() throws Exception {
        TicketRequest request = new TicketRequest("VIP", 500.0, 100, null, 1L);

        TicketResponse response = TicketResponse.builder()
                .id(1L)
                .name("VIP")
                .price(500.0)
                .quota(100)
                .category(TicketCategory.VIP)
                .soldOut(false)
                .eventId(1L)
                .build();

        when(ticketService.addTicket(any(TicketRequest.class), any(User.class))).thenReturn(response);

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("VIP"))
                .andExpect(jsonPath("$.price").value(500.0));
    }

    @Test
    @WithMockUser(username = "organizer@example.com", roles = {"ORGANIZER"})
    void updateTicket_shouldReturnOk() throws Exception {
        TicketRequest request = new TicketRequest("Regular", 250.0, 40, null, 1L);

        TicketResponse response = TicketResponse.builder()
                .id(1L)
                .name("Regular")
                .price(250.0)
                .quota(40)
                .category(null) // Or TicketCategory.REGULAR if exists
                .soldOut(false)
                .eventId(1L)
                .build();

        when(ticketService.updateTicket(Mockito.eq(1L), any(TicketRequest.class), any(User.class))).thenReturn(response);

        mockMvc.perform(put("/api/tickets/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(250.0));
    }

    @Test
    @WithMockUser(username = "attendee@example.com", roles = {"ATTENDEE"})
    void listAvailableTickets_shouldReturnList() throws Exception {
        TicketResponse ticket1 = TicketResponse.builder()
                .id(1L)
                .name("VIP")
                .price(500.0)
                .quota(100)
                .category(TicketCategory.VIP)
                .soldOut(false)
                .eventId(1L)
                .build();

        TicketResponse ticket2 = TicketResponse.builder()
                .id(2L)
                .name("Regular")
                .price(250.0)
                .quota(40)
                .category(null) // Or TicketCategory.REGULAR
                .soldOut(false)
                .eventId(1L)
                .build();

        when(ticketService.getAvailableTickets()).thenReturn(List.of(ticket1, ticket2));

        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("VIP"));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void deleteTicket_shouldReturnSuccess() throws Exception {
        User admin = new User();
        admin.setEmail("admin@example.com");
        admin.setRole(Role.ADMIN);

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        // ticketService.deleteTicket returns void, no need to mock explicitly

        mockMvc.perform(delete("/api/tickets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Ticket deleted successfully"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
