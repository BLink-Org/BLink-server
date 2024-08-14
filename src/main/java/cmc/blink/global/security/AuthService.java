package cmc.blink.global.security;

import cmc.blink.domain.link.business.LinkService;
import cmc.blink.domain.user.business.UserMapper;
import cmc.blink.domain.user.implement.UserCommandAdapter;
import cmc.blink.domain.user.implement.UserQueryAdapter;
import cmc.blink.domain.user.persistence.Role;
import cmc.blink.domain.user.persistence.User;
import cmc.blink.domain.user.persistence.redis.BlackListToken;
import cmc.blink.domain.user.persistence.redis.BlackListTokenRepository;
import cmc.blink.domain.user.persistence.redis.RefreshToken;
import cmc.blink.domain.user.persistence.redis.RefreshTokenRepository;
import cmc.blink.global.exception.JwtAuthenticationException;
import cmc.blink.global.exception.constant.ErrorCode;
import cmc.blink.global.security.client.AppleAuthClient;
import cmc.blink.global.security.client.GoogleTokenVerifierClient;
import cmc.blink.global.security.dto.AuthRequest;
import cmc.blink.global.security.dto.AuthResponse;
import cmc.blink.global.security.dto.GoogleUserInfo;
import cmc.blink.global.security.dto.Token;
import cmc.blink.global.security.provider.ApplePublicKeyGenerator;
import cmc.blink.global.security.provider.JwtValidator;
import cmc.blink.global.security.provider.TokenProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final GoogleTokenVerifierClient googleTokenVerifierClient;
    private final AppleAuthClient appleAuthClient;

    private final TokenProvider tokenProvider;
    private final JwtValidator jwtValidator;
    private final ApplePublicKeyGenerator applePublicKeyGenerator;

    private final UserQueryAdapter userQueryAdapter;
    private final UserCommandAdapter userCommandAdapter;
    private final LinkService linkService;

    private final BlackListTokenRepository blackListTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.token-valid-time}")
    private int tokenValidSeconds;

    @Value("${jwt.refresh-valid-time}")
    private int refreshValidSeconds;

    @Transactional
    public AuthResponse.LoginResponseDto googleLogin(AuthRequest.GoogleLoginRequestDto requestDto) {

        GoogleUserInfo userInfo = googleTokenVerifierClient.verifyIdToken(requestDto.getIdToken());

        Optional<User> optionalUser = userQueryAdapter.findByEmail(userInfo.getEmail());

        User user;

        if(optionalUser.isEmpty()){
            user = UserMapper.toUser(userInfo, "google");
            user.updateLoginTime();
            userCommandAdapter.save(user);

            linkService.saveDefaultLink(user);
        }else{
            user = optionalUser.get();
            user.update(userInfo.getName(), userInfo.getEmail(), "google");

            user.updateLoginTime();
            userCommandAdapter.save(user);
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                Collections.singleton(new SimpleGrantedAuthority(Role.USER.getKey()))
        );

        Token token = tokenProvider.generateToken(authentication);

        return AuthMapper.toLoginResponseDto(token);
    }

    @Transactional
    public AuthResponse.LoginResponseDto appleLogin(AuthRequest.AppleLoginRequestDto requestDto) throws NoSuchAlgorithmException, InvalidKeySpecException, JsonProcessingException {
        String accountId = getAppleAccountId(requestDto.getIdentityToken());

        Optional<User> optionalUser = userQueryAdapter.findByEmail(requestDto.getEmail());

        User user;

        if(optionalUser.isEmpty()){
            user = UserMapper.toUser(requestDto, "apple");
            user.updateLoginTime();
            userCommandAdapter.save(user);

            linkService.saveDefaultLink(user);
        }else{
            user = optionalUser.get();
            user.update(requestDto.getEmail(), "apple");
            user.updateLoginTime();
            userCommandAdapter.save(user);
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                Collections.singleton(new SimpleGrantedAuthority(Role.USER.getKey()))
        );

        Token token = tokenProvider.generateToken(authentication);

        return AuthMapper.toLoginResponseDto(token);

    }

    public String getAppleAccountId(String identityToken)
            throws JsonProcessingException, AuthenticationException, NoSuchAlgorithmException,
            InvalidKeySpecException {
        Map headers = jwtValidator.parseHeaders(identityToken);
        
        PublicKey publicKey = applePublicKeyGenerator.generatePublicKey(headers,
                appleAuthClient.getAppleAuthPublicKey());

        return jwtValidator.getTokenClaims(identityToken, publicKey).getSubject();
    }

    @Transactional
    public void logout(HttpServletRequest request, AuthRequest.LogoutRequestDto logoutRequestDto, User user) {
        String accessToken = tokenProvider.resolveToken(request);
        String refreshToken = logoutRequestDto.getRefreshToken();

        blackListTokenRepository.save(BlackListToken.builder()
                .blackListToken(accessToken)
                .expiration((long) tokenValidSeconds)
                .build());

        RefreshToken optionalRefreshToken = refreshTokenRepository
                .findById(refreshToken).orElseThrow(()-> new JwtAuthenticationException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        refreshTokenRepository.delete(optionalRefreshToken);
    }

    @Transactional
    public AuthResponse.ReissueResponseDto reissue(AuthRequest.ReissueRequestDto reissueRequestDto) {
        if (reissueRequestDto.getRefreshToken().isEmpty())
            throw new JwtAuthenticationException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);

        Token reissuedToken = tokenProvider.refreshToken(reissueRequestDto.getRefreshToken());

        return AuthMapper.toReissuedTokenResponseDto(reissuedToken);
    }
}
