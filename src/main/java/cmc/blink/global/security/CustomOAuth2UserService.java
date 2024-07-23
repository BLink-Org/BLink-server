package cmc.blink.global.security;

import cmc.blink.domain.user.business.UserMapper;
import cmc.blink.domain.user.implement.UserCommandAdapter;
import cmc.blink.domain.user.implement.UserQueryAdapter;
import cmc.blink.domain.user.persistence.Role;
import cmc.blink.domain.user.persistence.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserQueryAdapter userQueryAdapter;
    private final UserCommandAdapter userCommandAdapter;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = oAuth2UserService.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint()
                .getUserNameAttributeName();

        Map<String, Object> oAuth2UserAttributeMap = oAuth2User.getAttributes();
        OAuth2UserAttributes oAuth2UserAttributes = OAuth2UserAttributes.of(registrationId, userNameAttributeName, oAuth2UserAttributeMap);

        Optional<User> optionalUser = userQueryAdapter.findByEmail(oAuth2UserAttributes.getEmail());

        if(optionalUser.isEmpty()){
            User user = UserMapper.toUser(oAuth2UserAttributes);
            user.updateLoginTime();
            userCommandAdapter.save(user);
        }else{
            User user = optionalUser.get();
            user.update(oAuth2UserAttributes.getName(), oAuth2UserAttributes.getEmail(), oAuth2UserAttributes.getProvider());
            user.updateLoginTime();
            userCommandAdapter.save(user);
        }

        return new DefaultOAuth2User(Collections.singleton(new SimpleGrantedAuthority(Role.USER.getKey())), oAuth2UserAttributes.toMap(), "email");
    }
}
