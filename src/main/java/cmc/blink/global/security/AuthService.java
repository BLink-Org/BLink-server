package cmc.blink.global.security;

import cmc.blink.domain.user.business.UserMapper;
import cmc.blink.domain.user.implement.UserCommandAdapter;
import cmc.blink.domain.user.implement.UserQueryAdapter;
import cmc.blink.domain.user.persistence.Role;
import cmc.blink.domain.user.persistence.User;
import cmc.blink.global.security.client.GoogleTokenVerifierClient;
import cmc.blink.global.security.dto.AuthRequest;
import cmc.blink.global.security.dto.AuthResponse;
import cmc.blink.global.security.dto.GoogleUserInfo;
import cmc.blink.global.security.dto.Token;
import cmc.blink.global.security.provider.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final GoogleTokenVerifierClient googleTokenVerifierClient;

    private final TokenProvider tokenProvider;

    private final UserQueryAdapter userQueryAdapter;
    private final UserCommandAdapter userCommandAdapter;

    @Transactional
    public AuthResponse.LoginResponseDto googleLogin(AuthRequest.GoogleLoginRequestDto requestDto) {

        GoogleUserInfo userInfo = googleTokenVerifierClient.verifyIdToken(requestDto.getIdToken());

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
