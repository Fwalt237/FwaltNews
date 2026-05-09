package com.mjc.school.service;

import com.mjc.school.service.security.dto.SignupRequest;

public interface UserService {
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    void registerUser(SignupRequest request);
}
