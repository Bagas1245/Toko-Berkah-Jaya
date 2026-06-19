package com.mycompany.tokoberkahjayaa.process;

import com.mycompany.tokoberkahjayaa.model.User;
import com.mycompany.tokoberkahjayaa.data.UserRepository;

public class AuthService {

    private final UserRepository userRepo;

    public AuthService() {
        this.userRepo = new UserRepository();
    }

    public User login(String username, String password) {
        if (username == null || username.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            return null;
        }
        return userRepo.findByUsernameAndPassword(username.trim(), password.trim());
    }

    public boolean validateInput(String username, String password) {
        return username != null && !username.trim().isEmpty() &&
               password != null && !password.trim().isEmpty();
    }
}
