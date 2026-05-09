package com.mjc.school.controller.impl;

import com.mjc.school.service.UserService;
import com.mjc.school.service.security.MyUser;
import com.mjc.school.service.security.dto.AuthResponse;
import com.mjc.school.service.security.dto.LoginRequest;
import com.mjc.school.service.security.dto.SignupRequest;
import com.mjc.school.service.security.jwt.JwtUtil;
import com.mjc.school.versioning.ApiVersion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static com.mjc.school.controller.RestApiConst.AUTHENTICATION_API_ROOT_PATH;


@ApiVersion(1)
@RestController
@RequestMapping(value=AUTHENTICATION_API_ROOT_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthenticationController {

    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationController(JwtUtil jwtUtil,UserService userService,AuthenticationManager authenticationManager){
        this.jwtUtil=jwtUtil;
        this.userService=userService;
        this.authenticationManager=authenticationManager;
    }


    @Operation(summary = "Sign up a new user", description = "Creates a new user account and returns a JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully signed up"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "Username or email already taken")
    })
    @PostMapping("/signup")
    public ResponseEntity<Object> signup(@RequestBody SignupRequest request){
        if(userService.existsByUsername(request.username())){
            return ResponseEntity.badRequest().body(Map.of("message","Username already in use."));
        }

        if(userService.existsByEmail(request.email())){
            return ResponseEntity.badRequest().body(Map.of("message","Email already in use."));
        }

        userService.registerUser(request);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),request.password())
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        MyUser myUser = (MyUser) authentication.getPrincipal();
        String jwt = jwtUtil.generateToken(myUser);

        List<String> roles = myUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(jwt,roles,myUser.getEmail(),myUser.getUsername()));

    }

    @Operation(summary = "Log in", description = "Authenticates a user and returns a JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully logged in"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody LoginRequest request){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),request.password())
        );

        MyUser myUser = (MyUser) authentication.getPrincipal();
        String jwt = jwtUtil.generateToken(myUser);

        List<String> roles = myUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return ResponseEntity.ok()
                .body(new AuthResponse(jwt,roles,myUser.getEmail(),myUser.getUsername()));

    }
}
