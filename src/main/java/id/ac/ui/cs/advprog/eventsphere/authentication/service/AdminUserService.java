package id.ac.ui.cs.advprog.eventsphere.authentication.service;

import id.ac.ui.cs.advprog.eventsphere.authentication.dto.UpdateUserRequest;
import id.ac.ui.cs.advprog.eventsphere.authentication.dto.UserResponseDTO;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.Role;

import java.util.List;

public interface AdminUserService {
    List<UserResponseDTO> getAllUsers();
    UserResponseDTO getUserById(Long id);
    UserResponseDTO updateUser(Long id, UpdateUserRequest updateRequest);
    void deleteUser(Long id);
    UserResponseDTO updateOwnProfile(UpdateUserRequest updateRequest);
    List<UserResponseDTO> getUsersByRole(Role role);
}