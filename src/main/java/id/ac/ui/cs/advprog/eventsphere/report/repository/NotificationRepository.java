package id.ac.ui.cs.advprog.eventsphere.report.repository;

import id.ac.ui.cs.advprog.eventsphere.report.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByRecipientId(Long recipientId);

    List<Notification> findByRecipientEmail(String email);

    List<Notification> findByRecipientIdAndReadOrderByCreatedAtDesc(Long recipientId, boolean read);

    List<Notification> findByRecipientEmailAndReadOrderByCreatedAtDesc(String email, boolean read);

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    List<Notification> findByRecipientEmailOrderByCreatedAtDesc(String email);

    List<Notification> findByRelatedEntityId(UUID relatedEntityId);

    long countByRecipientIdAndRead(Long recipientId, boolean read);

    long countByRecipientEmailAndRead(String email, boolean read);
}