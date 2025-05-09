package id.ac.ui.cs.advprog.eventsphere.authentication.dto;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JwtResponse {
    private String token;
    private String email;
    private Role role;
}
