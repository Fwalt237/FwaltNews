package com.mjc.school.service.security.oauth2;

import com.mjc.school.repository.impl.UserRepository;
import com.mjc.school.repository.model.user.AuthProvider;
import com.mjc.school.repository.model.user.Role;
import com.mjc.school.repository.model.user.User;
import com.mjc.school.service.exceptions.OAuth2AuthenticationProcessingException;
import com.mjc.school.service.security.MyUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;

@Service
public class MyOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Autowired
    public MyOAuth2UserService(UserRepository userRepository){
        this.userRepository=userRepository;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        try{
            return processOAuth2User(userRequest, oauth2User);
        } catch(AuthenticationException ae){
            throw ae;
        }catch(Exception e){
            throw new InternalAuthenticationServiceException(e.getMessage(),e.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest,OAuth2User oauth2User){
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId,oauth2User.getAttributes());
        String email = userInfo.getEmail();
        if(!StringUtils.hasText(email)){
            email = userInfo.getName() + "@"+registrationId+".com";
        }

        Optional<User> optionalUser = userRepository.findByEmail(email);
        User user ;

        if(optionalUser.isPresent()){
            user = optionalUser.get();
            if(!user.getProvider().equals(AuthProvider.valueOf(registrationId.toUpperCase()))){
                throw new OAuth2AuthenticationProcessingException(
                        "Please use your "+user.getProvider()+" account");
            }
            user = updateExistingUser(user,userInfo);
        }else{
            user = registerNewUser(userRequest,userInfo,email);
        }
        return new MyUser(user,oauth2User.getAttributes());
    }

    private User updateExistingUser(User user, OAuth2UserInfo userInfo){
        user.setFirstName(userInfo.getName());
        return userRepository.save(user);
    }

    private User registerNewUser(OAuth2UserRequest userRequest, OAuth2UserInfo userInfo, String email){
        User user = new User();

        user.setProvider(AuthProvider.valueOf(userRequest.getClientRegistration().getRegistrationId().toUpperCase()));
        user.setProviderId(userInfo.getId());
        user.setEmail(email);
        user.setPassword(UUID.randomUUID().toString());
        user.getRoles().add(Role.ROLE_USER);
        extractedUsername(email, user);
        extractedFirstAndLastName(userInfo, user);
        return userRepository.save(user);
    }

    private void extractedFirstAndLastName(OAuth2UserInfo userInfo, User user) {
        String fullName = userInfo.getName();
        if(!StringUtils.hasText(fullName)){
            user.setFirstName("User");
        }
        String[] nameParts = fullName.trim().split("\\s+");
        if (nameParts.length>0){
            user.setFirstName(nameParts[0]);
        }
        if (nameParts.length>1){
            user.setLastName(fullName.substring(fullName.indexOf(nameParts[1])).trim());
        }
    }

    private void extractedUsername(String email, User user) {
        String uniqueUsername = email.split("@")[0];
        int count =1;
        while(userRepository.existsByUsername(uniqueUsername)){
            uniqueUsername=uniqueUsername+count;
            count++;
        }
        user.setUsername(uniqueUsername);
    }
}
