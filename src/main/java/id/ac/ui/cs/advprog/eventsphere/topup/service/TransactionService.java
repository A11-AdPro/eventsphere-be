package id.ac.ui.cs.advprog.eventsphere.topup.service;

import id.ac.ui.cs.advprog.eventsphere.topup.dto.PurchaseRequestDTO;
import id.ac.ui.cs.advprog.eventsphere.topup.dto.TopUpResponseDTO;
import id.ac.ui.cs.advprog.eventsphere.topup.dto.TransactionDTO;
import id.ac.ui.cs.advprog.eventsphere.topup.model.Transaction;

import java.util.List;

public interface TransactionService {
    TopUpResponseDTO processTicketPurchase(PurchaseRequestDTO purchaseRequest);
    List<TransactionDTO> getAllTransactions();
    List<TransactionDTO> getUserTransactions(String userId);
    boolean deleteTransaction(String transactionId);
    boolean markTransactionAsFailed(String transactionId);
    TransactionDTO getTransactionById(String transactionId);
}