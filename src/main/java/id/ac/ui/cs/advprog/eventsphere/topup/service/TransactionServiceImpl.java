package id.ac.ui.cs.advprog.eventsphere.topup.service;

import id.ac.ui.cs.advprog.eventsphere.topup.dto.PurchaseRequestDTO;
import id.ac.ui.cs.advprog.eventsphere.topup.dto.TopUpResponseDTO;
import id.ac.ui.cs.advprog.eventsphere.topup.dto.TransactionDTO;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.topup.model.Transaction;
import id.ac.ui.cs.advprog.eventsphere.topup.repository.TransactionRepository;
import id.ac.ui.cs.advprog.eventsphere.authentication.repository.UserRepository;
import id.ac.ui.cs.advprog.eventsphere.topup.util.CurrentUserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final CurrentUserUtil currentUserUtil;

    @Autowired
    public TransactionServiceImpl(UserRepository userRepository,
                                  TransactionRepository transactionRepository,
                                  CurrentUserUtil currentUserUtil) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.currentUserUtil = currentUserUtil;
    }

    @Override
    @Transactional
    public TopUpResponseDTO processTicketPurchase(PurchaseRequestDTO purchaseRequest) {
        String email = currentUserUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Transaction transaction = Transaction.builder()
                .user(user)
                .amount(purchaseRequest.getAmount())
                .timestamp(LocalDateTime.now())
                .type(Transaction.TransactionType.TICKET_PURCHASE)
                .status(Transaction.TransactionStatus.PENDING)
                .description(purchaseRequest.getDescription())
                .eventId(purchaseRequest.getEventId())
                .build();

        // Check if user has enough balance
        boolean successful = user.deductBalance(purchaseRequest.getAmount());

        if (successful) {
            transaction.setStatus(Transaction.TransactionStatus.SUCCESS);
            userRepository.save(user);
        } else {
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transaction.setDescription("Failed: Insufficient balance");
        }

        transaction = transactionRepository.save(transaction);

        return TopUpResponseDTO.builder()
                .transactionId(transaction.getId())
                .userId(user.getId())
                .amount(purchaseRequest.getAmount())
                .newBalance(user.getBalance())
                .timestamp(transaction.getTimestamp())
                .status(transaction.getStatus().toString())
                .build();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<TransactionDTO> getAllTransactions() {
        List<Transaction> transactions = transactionRepository.findAll();
        return mapTransactionsToDTO(transactions);
    }

    @Override
    public List<TransactionDTO> getCurrentUserTransactions() {
        String email = currentUserUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Transaction> transactions = transactionRepository.findByUser(user);
        return mapTransactionsToDTO(transactions);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<TransactionDTO> getUserTransactions(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Transaction> transactions = transactionRepository.findByUser(user);
        return mapTransactionsToDTO(transactions);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public boolean deleteTransaction(String transactionId) {
        if (transactionRepository.existsById(transactionId)) {
            transactionRepository.deleteById(transactionId);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public boolean markTransactionAsFailed(String transactionId) {
        return transactionRepository.findById(transactionId)
                .map(transaction -> {
                    transaction.setStatus(Transaction.TransactionStatus.FAILED);
                    transactionRepository.save(transaction);
                    return true;
                })
                .orElse(false);
    }

    @Override
    public TransactionDTO getTransactionById(String transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        
        // Check if the requesting user is the owner of this transaction or an admin
        String currentUserEmail = currentUserUtil.getCurrentUserEmail();
        User transactionUser = transaction.getUser();
        
        if (!transactionUser.getEmail().equals(currentUserEmail)) {
            // If not the owner, check if the user has admin role
            // Throwing an exception because non-admin users should not see others' transactions
            throw new RuntimeException("Access denied: You can only view your own transactions");
        }

        return mapTransactionToDTO(transaction);
    }

    private List<TransactionDTO> mapTransactionsToDTO(List<Transaction> transactions) {
        return transactions.stream()
                .map(this::mapTransactionToDTO)
                .collect(Collectors.toList());
    }

    private TransactionDTO mapTransactionToDTO(Transaction transaction) {
        return TransactionDTO.builder()
                .id(transaction.getId())
                .userId(transaction.getUser().getId())
                .username(transaction.getUser().getUsername())
                .amount(transaction.getAmount())
                .timestamp(transaction.getTimestamp())
                .type(transaction.getType().toString())
                .status(transaction.getStatus().toString())
                .description(transaction.getDescription())
                .eventId(transaction.getEventId())
                .build();
    }
}