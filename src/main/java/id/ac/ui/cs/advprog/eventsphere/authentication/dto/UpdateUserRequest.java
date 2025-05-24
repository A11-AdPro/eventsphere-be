package id.ac.ui.cs.advprog.eventsphere.authentication.dto;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.Role;
import lombok.Data;

@Data
public class UpdateUserRequest {
    private String email;
    private String fullName;
    private Role role; // Optional
    private Integer balance; // Optional
    private String password; // Optional
}