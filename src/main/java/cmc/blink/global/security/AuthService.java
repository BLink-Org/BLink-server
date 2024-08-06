package cmc.blink.global.security;

import cmc.blink.domain.user.business.UserMapper;
import cmc.blink.domain.user.implement.UserCommandAdapter;
import cmc.blink.domain.user.implement.UserQueryAdapter;
import cmc.blink.domain.user.persistence.Role;
import cmc.blink.domain.user.persistence.User;
import cmc.blink.global.security.client.GoogleOauth2Client;
import cmc.blink.global.security.client.GoogleUserFeignClient;
import cmc.blink.global.security.dto.AuthResponse;
import cmc.blink.global.security.dto.GoogleUserInfo;
import cmc.blink.global.security.dto.Token;
import cmc.blink.global.security.provider.TokenProvider;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String GOOGLE_CLIENT_ID;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String GOOGLE_CLIENT_SECRET;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String GOOGLE_REDIRECT_URL;

    @Value("${spring.security.oauth2.client.registration.google.authorization-grant-type}")
    private String GOOGLE_GRANT_TYPE;

    private final GoogleOauth2Client googleOauth2Client;
    private final GoogleUserFeignClient googleUserFeignClient;

    private final TokenProvider tokenProvider;

    private final UserQueryAdapter userQueryAdapter;
    private final UserCommandAdapter userCommandAdapter;

    @Transactional
    public AuthResponse.LoginResponseDto googleLogin(String code) {
        JsonNode jsonNode = googleOauth2Client.getAccessToken(code, GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET, GOOGLE_REDIRECT_URL, GOOGLE_GRANT_TYPE);

        String accessToken = jsonNode.get("access_token").asText();

        GoogleUserInfo userInfo = googleUserFeignClient.getUserInfo("Bearer "+accessToken);
        
        Optional<User> optionalUser = userQueryAdapter.findByEmail(userInfo.getEmail());

        User user;

        if(optionalUser.isEmpty()){
            user = UserMapper.toUser(userInfo, "google");
        }else{
            user = optionalUser.get();
            user.update(userInfo.getName(), userInfo.getEmail(), "google");
        }
        user.updateLoginTime();
        userCommandAdapter.save(user);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                Collections.singleton(new SimpleGrantedAuthority(Role.USER.getKey()))
        );

        Token token = tokenProvider.generateToken(authentication);

        return AuthResponse.LoginResponseDto.builder()
                .accessToken(token.getAccessToken())
                .refreshToken(token.getRefreshToken())
                .build();

    }
}
