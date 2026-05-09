package com.mjc.school.repository;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordTest {

    @Test
    void generateHash(){
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String adminHash = encoder.encode("admin@example.com");
        String userHash = encoder.encode("user@example.com");

        assertNotNull(adminHash, "Encoded admin password should not be null");
        assertNotNull(userHash, "Encoded user password should not be null");
        assertTrue(adminHash.startsWith("$2"), "Hash should follow BCrypt format");

        System.out.println("Admin Hash: " + adminHash);
        System.out.println("User Hash: " + userHash);
    }
}
