package id.ac.ui.cs.advprog.eventsphere.topup.service;

import id.ac.ui.cs.advprog.eventsphere.topup.dto.TopUpResponseDTO;
import id.ac.ui.cs.advprog.eventsphere.topup.dto.TransactionDTO;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.topup.model.Transaction;
import id.ac.ui.cs.advprog.eventsphere.topup.repository.TransactionRepository;
import id.ac.ui.cs.advprog.eventsphere.authentication.repository.UserRepository;
import id.ac.ui.cs.advprog.eventsphere.ticket.dto.TicketResponse;
import id.ac.ui.cs.advprog.eventsphere.ticket.service.TicketService;
import id.ac.ui.cs.advprog.eventsphere.topup.util.CurrentUserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final CurrentUserUtil currentUserUtil;
    private final TicketService ticketService;


    @Autowired
    public TransactionServiceImpl(UserRepository userRepository,
                                  TransactionRepository transactionRepository,
                                  CurrentUserUtil currentUserUtil,
                                  TicketService ticketService) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.currentUserUtil = currentUserUtil;
        this.ticketService = ticketService;
    }

    @Override
    @Transactional
    public TopUpResponseDTO processTicketPurchaseById(Long ticketId) {
        String email = currentUserUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        TicketResponse ticketInfo = ticketService.getTicketById(ticketId);
        
        if (ticketInfo.isSoldOut()) {
            throw new RuntimeException("Ticket is sold out");
        }
        
        int ticketPrice = (int) ticketInfo.getPrice();
        if (user.getBalance() < ticketPrice) {
            Transaction transaction = Transaction.builder()
                    .user(user)
                    .amount(ticketPrice)
                    .timestamp(LocalDateTime.now())
                    .type(Transaction.TransactionType.TICKET_PURCHASE)
                    .status(Transaction.TransactionStatus.FAILED)
                    .description("Failed: Insufficient balance")
                    .eventId(String.valueOf(ticketInfo.getEventId()))
                    .build();
            
            transactionRepository.save(transaction);
            throw new RuntimeException("Insufficient balance");
        }
        
        TicketResponse updatedTicket = ticketService.purchaseTicket(ticketId);
        
        user.deductBalance(ticketPrice);
        userRepository.save(user);
        
        Transaction transaction = Transaction.builder()
                .user(user)
                .amount(ticketPrice)
                .timestamp(LocalDateTime.now())
                .type(Transaction.TransactionType.TICKET_PURCHASE)
                .status(Transaction.TransactionStatus.SUCCESS)
                .description("Purchase ticket: " + updatedTicket.getName())
                .eventId(String.valueOf(updatedTicket.getEventId()))
                .build();
        
        transaction = transactionRepository.save(transaction);
        
        return TopUpResponseDTO.builder()
                .transactionId(transaction.getId())
                .userId(user.getId())
                .amount(ticketPrice)
                .newBalance(user.getBalance())
                .timestamp(transaction.getTimestamp())
                .status(transaction.getStatus().toString())
                .build();
    }

    // Async method
    @Async("taskExecutor")
    public CompletableFuture<TopUpResponseDTO> processTicketPurchaseByIdAsync(Long ticketId) {
        try {
            TopUpResponseDTO response = processTicketPurchaseById(ticketId);
            return CompletableFuture.completedFuture(response);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
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