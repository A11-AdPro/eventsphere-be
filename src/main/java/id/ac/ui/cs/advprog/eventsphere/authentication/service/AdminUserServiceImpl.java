package id.ac.ui.cs.advprog.eventsphere.authentication.service;

import id.ac.ui.cs.advprog.eventsphere.authentication.dto.UpdateUserRequest;
import id.ac.ui.cs.advprog.eventsphere.authentication.dto.UserResponseDTO;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.Role;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return toResponseDTO(user);
    }

    @Override
    @Transactional
    public UserResponseDTO updateUser(Long id, UpdateUserRequest updateRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (updateRequest.getEmail() != null && !updateRequest.getEmail().trim().isEmpty()) {
            if (userRepository.existsByEmail(updateRequest.getEmail()) && 
                !user.getEmail().equals(updateRequest.getEmail())) {
                throw new RuntimeException("Email already in use");
            }
            user.setEmail(updateRequest.getEmail().trim());
        }

        if (updateRequest.getFullName() != null && !updateRequest.getFullName().trim().isEmpty()) {
            user.setFullName(updateRequest.getFullName().trim());
        }

        if (updateRequest.getRole() != null) {
            user.setRole(updateRequest.getRole());
        }

        if (updateRequest.getBalance() != null) {
            user.setBalance(updateRequest.getBalance());
        }

        if (updateRequest.getPassword() != null && !updateRequest.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
        }

        User savedUser = userRepository.save(user);
        return toResponseDTO(savedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = ((UserDetails) auth.getPrincipal()).getUsername();
        
        if (user.getEmail().equals(currentUserEmail)) {
            throw new RuntimeException("Cannot delete your own account");
        }

        userRepository.delete(user);
    }

    @Override
    @Transactional
    public UserResponseDTO updateOwnProfile(UpdateUserRequest updateRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = ((UserDetails) auth.getPrincipal()).getUsername();
        
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        if (updateRequest.getEmail() != null && !updateRequest.getEmail().trim().isEmpty()) {
            if (userRepository.existsByEmail(updateRequest.getEmail()) && 
                !user.getEmail().equals(updateRequest.getEmail())) {
                throw new RuntimeException("Email already in use");
            }
            user.setEmail(updateRequest.getEmail().trim());
        }

        if (updateRequest.getFullName() != null && !updateRequest.getFullName().trim().isEmpty()) {
            user.setFullName(updateRequest.getFullName().trim());
        }

        if (updateRequest.getPassword() != null && !updateRequest.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
        }

        if (updateRequest.getRole() != null) {
            user.setRole(updateRequest.getRole());
        }

        User savedUser = userRepository.save(user);
        return toResponseDTO(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getUsersByRole(Role role) {
        return userRepository.findAll()
                .stream()
                .filter(user -> user.getRole() == role)
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    private UserResponseDTO toResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .balance(user.getBalance())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}