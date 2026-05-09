package com.mjc.school.service.impl;
import com.mjc.school.repository.impl.UserRepository;
import com.mjc.school.repository.model.user.Role;
import com.mjc.school.repository.model.user.User;
import com.mjc.school.service.UserService;
import com.mjc.school.service.security.dto.SignupRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder encoder){
        this.userRepository=userRepository;
        this.encoder=encoder;
    }
    @Override
    @Transactional(readOnly=true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly=true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public void registerUser(SignupRequest request) {

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPassword(encoder.encode(request.password()));
        user.getRoles().add(Role.ROLE_USER);

        userRepository.save(user);
    }
}
