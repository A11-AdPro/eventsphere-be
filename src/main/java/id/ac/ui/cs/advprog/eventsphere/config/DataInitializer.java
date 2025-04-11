package id.ac.ui.cs.advprog.eventsphere.config;

import id.ac.ui.cs.advprog.eventsphere.model.User;
import id.ac.ui.cs.advprog.eventsphere.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Profile("!prod")
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @PostConstruct
    public void init() {
        // Create a test organizer if none exists
        if (userRepository.count() == 0) {
            User organizer = User.builder()
                    .username("organizer")
                    .password(passwordEncoder.encode("password"))
                    .name("Test Organizer")
                    .email("organizer@example.com")
                    .role(null)
                    .build();
            
            userRepository.save(organizer);
        }
    }
}