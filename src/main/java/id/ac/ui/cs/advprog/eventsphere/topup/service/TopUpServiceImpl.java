package id.ac.ui.cs.advprog.eventsphere.topup.service;

import id.ac.ui.cs.advprog.eventsphere.topup.dto.TopUpRequestDTO;
import id.ac.ui.cs.advprog.eventsphere.topup.dto.TopUpResponseDTO;
import id.ac.ui.cs.advprog.eventsphere.topup.entity.User;
import id.ac.ui.cs.advprog.eventsphere.topup.model.*;
import id.ac.ui.cs.advprog.eventsphere.topup.repository.TransactionRepository;
import id.ac.ui.cs.advprog.eventsphere.topup.repository.UserRepository;
import id.ac.ui.cs.advprog.eventsphere.topup.strategy.TopUpFactory;
import id.ac.ui.cs.advprog.eventsphere.topup.strategy.TopUpStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TopUpServiceImpl implements TopUpService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final TopUpStrategy topUpStrategy;
    private final TopUpFactory topUpFactory;

    @Autowired
    public TopUpServiceImpl(UserRepository userRepository,
                            TransactionRepository transactionRepository,
                            TopUpStrategy topUpStrategy,
                            TopUpFactory topUpFactory) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.topUpStrategy = topUpStrategy;
        this.topUpFactory = topUpFactory;
    }

    @Override
    @Transactional
    public TopUpResponseDTO processTopUp(TopUpRequestDTO topUpRequest) {
        User user = userRepository.findById(topUpRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        TopUp topUp;
        try {
            topUp = topUpFactory.createTopUp(topUpRequest.getTopUpType(), topUpRequest.getAmount());

            topUpStrategy.executeTopUp(user, topUp);
            userRepository.save(user);

            Transaction transaction = Transaction.builder()
                    .user(user)
                    .amount(topUp.getAmount())
                    .timestamp(LocalDateTime.now())
                    .type(Transaction.TransactionType.TOP_UP)
                    .status(Transaction.TransactionStatus.SUCCESS)
                    .description("Top-up " + topUp.getType() + " - IDR " + topUp.getAmount())
                    .build();

            transaction = transactionRepository.save(transaction);

            return TopUpResponseDTO.builder()
                    .transactionId(transaction.getId())
                    .userId(user.getId())
                    .amount(topUp.getAmount())
                    .newBalance(user.getBalance())
                    .timestamp(transaction.getTimestamp())
                    .status(transaction.getStatus().toString())
                    .build();

        } catch (IllegalArgumentException e) {
            Transaction transaction = Transaction.builder()
                    .user(user)
                    .amount(topUpRequest.getAmount())
                    .timestamp(LocalDateTime.now())
                    .type(Transaction.TransactionType.TOP_UP)
                    .status(Transaction.TransactionStatus.FAILED)
                    .description("Failed: " + e.getMessage())
                    .build();

            transaction = transactionRepository.save(transaction);

            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public List<Transaction> getUserTopUpTransactions(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return transactionRepository.findByUserAndType(user, Transaction.TransactionType.TOP_UP);
    }

    @Override
    public User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}