package id.ac.ui.cs.advprog.eventsphere.authentication.service;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserBalanceService {
    
    private final UserRepository userRepository;
    
    @Autowired
    public UserBalanceService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Transactional
    public void topUp(User user, int amount) {
        user.topUp(amount);
        userRepository.save(user);
    }
    
    @Transactional
    public boolean deductBalance(User user, int amount) {
        boolean success = user.deductBalance(amount);
        if (success) {
            userRepository.save(user);
        }
        return success;
    }
    
    @Transactional(readOnly = true)
    public int getBalance(User user) {
        return user.getBalance();
    }
}