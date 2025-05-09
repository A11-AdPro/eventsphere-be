package id.ac.ui.cs.advprog.eventsphere.topup.service;

import id.ac.ui.cs.advprog.eventsphere.topup.dto.TopUpRequestDTO;
import id.ac.ui.cs.advprog.eventsphere.topup.dto.TopUpResponseDTO;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.topup.model.Transaction;

import java.util.List;

public interface TopUpService {
    TopUpResponseDTO processTopUp(TopUpRequestDTO topUpRequest);
    List<Transaction> getCurrentUserTopUpTransactions();
    List<Transaction> getUserTopUpTransactions(Long userId); // Admin only method
    User getCurrentUserDetails();
    User getUserById(Long userId); // Admin only method
}