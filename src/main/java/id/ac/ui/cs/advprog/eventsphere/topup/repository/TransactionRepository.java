package id.ac.ui.cs.advprog.eventsphere.topup.repository;

import id.ac.ui.cs.advprog.eventsphere.topup.entity.User;
import id.ac.ui.cs.advprog.eventsphere.topup.model.Transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
    List<Transaction> findByUser(User user);
    List<Transaction> findByUserAndType(User user, Transaction.TransactionType type);
    List<Transaction> findByUserAndStatus(User user, Transaction.TransactionStatus status);
    List<Transaction> findByStatus(Transaction.TransactionStatus status);
}