package id.ac.ui.cs.advprog.eventsphere.authentication.service;

import id.ac.ui.cs.advprog.eventsphere.authentication.dto.*;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;

public interface AuthService {
    JwtResponse login(LoginRequest loginRequest);
    User register(RegisterRequest registerRequest);
    User getCurrentUser();
}
