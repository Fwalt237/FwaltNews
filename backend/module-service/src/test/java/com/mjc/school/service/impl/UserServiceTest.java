package com.mjc.school.service.impl;

import com.mjc.school.repository.impl.UserRepository;
import com.mjc.school.repository.model.user.User;
import com.mjc.school.service.security.dto.SignupRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;


import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Service Unit tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Should save an user with valid credentials")
    void registerUser_ShouldSaveUserWithValidCredentials(){
        SignupRequest request = new SignupRequest("john","pass","john@example.com","first","last");

        when(encoder.encode(anyString())).thenReturn("encrypted_pass");
        userService.registerUser(request);

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should return true when an user username exists")
    void existsByUsername_ShouldReturnTrueWhenUserExists(){
        when(userRepository.existsByUsername("existingUsername")).thenReturn(true);
        assertTrue(userService.existsByUsername("existingUsername"));
    }

    @Test
    @DisplayName("Should return true when an user email exists")
    void existsByEmail_ShouldReturnTrueWhenUserExists(){
        when(userRepository.existsByEmail("existingEmail")).thenReturn(true);
        assertTrue(userService.existsByEmail("existingEmail"));
    }

}
