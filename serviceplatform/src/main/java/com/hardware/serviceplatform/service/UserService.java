package com.hardware.serviceplatform.service;

import org.springframework.stereotype.Service;
import com.hardware.serviceplatform.entity.User;
import com.hardware.serviceplatform.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public User registerUser(User user) {
        if (emailExists(user.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        return userRepository.save(user);
    }

}
