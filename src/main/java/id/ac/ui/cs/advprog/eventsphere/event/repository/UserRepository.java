package id.ac.ui.cs.advprog.eventsphere.event.repository;

import id.ac.ui.cs.advprog.eventsphere.event.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}