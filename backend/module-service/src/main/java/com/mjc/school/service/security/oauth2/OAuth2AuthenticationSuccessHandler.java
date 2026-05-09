package com.mjc.school.service.security.oauth2;

import com.mjc.school.repository.impl.UserRepository;
import com.mjc.school.repository.model.user.AuthProvider;
import com.mjc.school.repository.model.user.Role;
import com.mjc.school.repository.model.user.User;
import com.mjc.school.service.security.MyUser;
import com.mjc.school.service.security.jwt.JwtUtil;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    private static final Logger log = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);

    @Autowired
    public OAuth2AuthenticationSuccessHandler(JwtUtil jwtUtil,UserRepository userRepository){
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        String targetUrl = determineTargetUrl(request,response,authentication);

        if(response.isCommitted()){
            log.debug("Response has already been committed. Unable to redirect to {}", targetUrl);
            return;
        }
        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) {

        String targetUrl = "http://localhost:3000/v1/news";
        Object principal = authentication.getPrincipal();

        String username;

        if (principal instanceof MyUser myUser) {
            username = myUser.getUsername();
        }
        else if (principal instanceof OAuth2User oAuth2User) {

            String registrationId = ((OAuth2AuthenticationToken) authentication)
                    .getAuthorizedClientRegistrationId();

            OAuth2UserInfo oauth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                    registrationId,
                    oAuth2User.getAttributes()
            );

            username = oauth2UserInfo.getEmail();
            if (username == null || username.isEmpty()) {
                username = oauth2UserInfo.getName();
            }
            persistNewUser(oauth2UserInfo, registrationId);
        } else {
            username = principal.toString();
        }

        String token = jwtUtil.generateToken(username, authentication.getAuthorities());

        return UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("token", token)
                .build().toUriString();
    }

    private void persistNewUser(OAuth2UserInfo userInfo, String registrationId) {
        String email = userInfo.getEmail();

        if (userRepository.findByEmail(email).isEmpty()) {
            log.info("Registering new OAuth2 user: {}", email);

            User newUser = new User();
            newUser.setEmail(email);
            newUser.setFirstName(userInfo.getName());
            newUser.setUsername(email);
            newUser.setEnabled(true);
            newUser.setAccountNonExpired(true);
            newUser.setAccountNonLocked(true);
            newUser.setCredentialsNonExpired(true);
            newUser.setPassword(UUID.randomUUID().toString());
            newUser.getRoles().add(Role.ROLE_USER);
            try {
                newUser.setProvider(AuthProvider.valueOf(registrationId.toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("Unknown provider {}, defaulting to LOCAL", registrationId);
                newUser.setProvider(AuthProvider.LOCAL);
            }
            userRepository.save(newUser);
        }
    }
}
