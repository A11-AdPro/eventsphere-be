package id.ac.ui.cs.advprog.eventsphere.authentication.dto;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponseDTO {
    private Long id;
    private String email;
    private String fullName;
    private Role role;
    private Integer balance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}