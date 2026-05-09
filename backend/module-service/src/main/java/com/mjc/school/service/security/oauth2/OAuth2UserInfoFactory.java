package com.mjc.school.service.security.oauth2;

import com.mjc.school.repository.model.user.AuthProvider;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

import java.util.Map;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId,
                                                   Map<String, Object> attributes){

        if(registrationId.equalsIgnoreCase(AuthProvider.GOOGLE.toString())){
            return new GoogleOAuth2UserInfo(attributes);
        }else if(registrationId.equalsIgnoreCase(AuthProvider.GITHUB.toString())){
            return new GithubOAuth2UserInfo(attributes);
        }else{
            OAuth2Error error = new OAuth2Error("unsupported_provider",
                    "Login with "+registrationId+" is not supported yet.",null);
            throw new OAuth2AuthenticationException(error);
        }
    }
}
