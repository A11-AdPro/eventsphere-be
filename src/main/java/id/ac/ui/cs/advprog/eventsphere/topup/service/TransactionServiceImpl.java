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
import org.springframework.transaction.annotation.Propagation; 

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
    @Transactional(propagation = Propagation.REQUIRED)
    public TopUpResponseDTO processTicketPurchaseById(Long ticketId) {
        try {
            if (ticketId == null || ticketId <= 0) {
                throw new IllegalArgumentException("Invalid ticket ID");
            }

            String email = currentUserUtil.getCurrentUserEmail();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            TicketResponse ticketInfo;
            try {
                ticketInfo = ticketService.getTicketById(ticketId);
                if (ticketInfo == null) {
                    throw new RuntimeException("Ticket not found");
                }
            } catch (Exception e) {
                Transaction failedTransaction = Transaction.builder()
                        .user(user)
                        .amount(0)
                        .timestamp(LocalDateTime.now())
                        .type(Transaction.TransactionType.TICKET_PURCHASE)
                        .status(Transaction.TransactionStatus.FAILED)
                        .description("Failed: Ticket not found - " + e.getMessage())
                        .eventId("N/A")
                        .build();
                transactionRepository.save(failedTransaction);
                throw new RuntimeException("Ticket not found: " + e.getMessage());
            }
            
            if (ticketInfo.isSoldOut()) {
                Transaction failedTransaction = Transaction.builder()
                        .user(user)
                        .amount((int) ticketInfo.getPrice())
                        .timestamp(LocalDateTime.now())
                        .type(Transaction.TransactionType.TICKET_PURCHASE)
                        .status(Transaction.TransactionStatus.FAILED)
                        .description("Failed: Ticket is sold out")
                        .eventId(String.valueOf(ticketInfo.getEventId()))
                        .build();
                transactionRepository.save(failedTransaction);
                throw new RuntimeException("Ticket is sold out");
            }
            
            int ticketPrice = (int) ticketInfo.getPrice();
            if (user.getBalance() < ticketPrice) {
                Transaction failedTransaction = Transaction.builder()
                        .user(user)
                        .amount(ticketPrice)
                        .timestamp(LocalDateTime.now())
                        .type(Transaction.TransactionType.TICKET_PURCHASE)
                        .status(Transaction.TransactionStatus.FAILED)
                        .description("Failed: Insufficient balance")
                        .eventId(String.valueOf(ticketInfo.getEventId()))
                        .build();
                transactionRepository.save(failedTransaction);
                throw new RuntimeException("Insufficient balance. Required: " + ticketPrice + 
                                         ", Available: " + user.getBalance());
            }
            
            TicketResponse updatedTicket;
            try {
                updatedTicket = ticketService.purchaseTicket(ticketId);
            } catch (Exception e) {
                Transaction failedTransaction = Transaction.builder()
                        .user(user)
                        .amount(ticketPrice)
                        .timestamp(LocalDateTime.now())
                        .type(Transaction.TransactionType.TICKET_PURCHASE)
                        .status(Transaction.TransactionStatus.FAILED)
                        .description("Failed: Unable to purchase ticket - " + e.getMessage())
                        .eventId(String.valueOf(ticketInfo.getEventId()))
                        .build();
                transactionRepository.save(failedTransaction);
                throw new RuntimeException("Failed to purchase ticket: " + e.getMessage());
            }
            
            if (!user.deductBalance(ticketPrice)) {
                Transaction failedTransaction = Transaction.builder()
                        .user(user)
                        .amount(ticketPrice)
                        .timestamp(LocalDateTime.now())
                        .type(Transaction.TransactionType.TICKET_PURCHASE)
                        .status(Transaction.TransactionStatus.FAILED)
                        .description("Failed: Unable to deduct balance")
                        .eventId(String.valueOf(updatedTicket.getEventId()))
                        .build();
                transactionRepository.save(failedTransaction);
                throw new RuntimeException("Failed to deduct balance");
            }
            
            userRepository.save(user);
            
            Transaction successTransaction = Transaction.builder()
                    .user(user)
                    .amount(ticketPrice)
                    .timestamp(LocalDateTime.now())
                    .type(Transaction.TransactionType.TICKET_PURCHASE)
                    .status(Transaction.TransactionStatus.SUCCESS)
                    .description("Purchase ticket: " + updatedTicket.getName())
                    .eventId(String.valueOf(updatedTicket.getEventId()))
                    .build();
            
            Transaction savedTransaction = transactionRepository.save(successTransaction);
            
            return TopUpResponseDTO.builder()
                    .transactionId(savedTransaction.getId())
                    .userId(user.getId())
                    .amount(ticketPrice)
                    .newBalance(user.getBalance())
                    .timestamp(savedTransaction.getTimestamp())
                    .status(savedTransaction.getStatus().toString())
                    .message("Ticket purchased successfully")
                    .build();
                    
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error during ticket purchase: " + e.getMessage(), e);
        }
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
    @Transactional(readOnly = true)
    public List<TransactionDTO> getAllTransactions() {
        List<Transaction> transactions = transactionRepository.findAll();
        return mapTransactionsToDTO(transactions);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDTO> getCurrentUserTransactions() {
        String email = currentUserUtil.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Transaction> transactions = transactionRepository.findByUser(user);
        return mapTransactionsToDTO(transactions);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public TransactionDTO getTransactionById(String transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        
        String currentUserEmail = currentUserUtil.getCurrentUserEmail();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
        
        User transactionUser = transaction.getUser();
        
        boolean isOwner = transactionUser.getId().equals(currentUser.getId());
        boolean isAdmin = "ADMIN".equals(currentUser.getRole().name());
        
        if (!isOwner && !isAdmin) {
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