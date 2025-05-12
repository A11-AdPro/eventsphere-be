package id.ac.ui.cs.advprog.eventsphere.topup.service;

import id.ac.ui.cs.advprog.eventsphere.topup.dto.TopUpResponseDTO;
import id.ac.ui.cs.advprog.eventsphere.topup.dto.TransactionDTO;

import java.util.List;

public interface TransactionService {
    TopUpResponseDTO processTicketPurchaseById(Long ticketId);
    List<TransactionDTO> getAllTransactions(); // Admin only
    List<TransactionDTO> getCurrentUserTransactions();
    List<TransactionDTO> getUserTransactions(Long userId); // Admin only
    boolean deleteTransaction(String transactionId);
    boolean markTransactionAsFailed(String transactionId);
    TransactionDTO getTransactionById(String transactionId);
}