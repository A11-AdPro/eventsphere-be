package id.ac.ui.cs.advprog.eventsphere.authentication.dto;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.Role;
import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String password;
    private String fullName;
    private Role role;
}
