package cmc.blink.global.security.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import cmc.blink.domain.user.implement.UserQueryAdapter;
import cmc.blink.domain.user.persistence.redis.BlackListToken;
import cmc.blink.domain.user.persistence.redis.BlackListTokenRepository;
import cmc.blink.domain.user.persistence.redis.RefreshToken;
import cmc.blink.domain.user.persistence.redis.RefreshTokenRepository;
import cmc.blink.global.exception.JwtAuthenticationException;
import cmc.blink.global.exception.constant.ErrorCode;
import cmc.blink.global.security.dto.Token;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
@Slf4j
public class TokenProvider {

    @Value("${jwt.token-valid-time}")
    private int tokenValidSeconds;
    @Value("${jwt.refresh-valid-time}")
    private int refreshValidSeconds;
    @Value("${jwt.secret-key}")
    private String secretValue;

    private int tokenValidMillis;
    private int refreshValidMillis;
    private String secretKey;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final BlackListTokenRepository blackListTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserQueryAdapter userQueryAdapter;

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretValue.getBytes());
        tokenValidMillis = tokenValidSeconds * 1000;
        refreshValidMillis = refreshValidSeconds * 1000;

        log.debug("Initialized with secretKey: {}", secretKey);
    }

    private SecretKey getSignKey(String secretKey) {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }


    public Token generateToken(Authentication authentication) {

        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Date now = new Date();

        String email = authentication.getName();

        String accessToken = Jwts.builder()
                .setSubject(email)
                .claim("auth", authorities)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + tokenValidMillis))
                .signWith(this.getSignKey(secretKey))
                .compact();

        String refreshToken = Jwts.builder()
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshValidMillis))
                .signWith(this.getSignKey(secretKey))
                .compact();

        refreshTokenRepository.save(RefreshToken.builder()
                .refreshToken(refreshToken)
                .email(email)
                .authorities(authorities)
                .expiration((long) refreshValidSeconds)
                .build());

        return new Token(accessToken, refreshToken);
    }

    public Token refreshToken(String refreshToken) throws JwtAuthenticationException {

        // 해당 리프레쉬 토큰이 레디스에 존재하는지 확인
        RefreshToken optionalRefreshToken = refreshTokenRepository.findById(refreshToken)
                .orElseThrow(() -> new JwtAuthenticationException(ErrorCode.INVALID_REFRESH_TOKEN));

        Date now = new Date();

        // 존재하면 토큰 새로 생성해서 반환
        String accessToken = Jwts.builder()
                .setSubject(optionalRefreshToken.getEmail())
                .claim("auth", optionalRefreshToken.getAuthorities())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + tokenValidMillis))
                .signWith(this.getSignKey(secretKey))
                .compact();

        String newRefreshToken = Jwts.builder()
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshValidMillis))
                .signWith(this.getSignKey(secretKey))
                .compact();

        refreshTokenRepository.save(RefreshToken.builder()
                .refreshToken(newRefreshToken)
                .email(optionalRefreshToken.getEmail())
                .authorities(optionalRefreshToken.getAuthorities())
                .expiration((long) refreshValidSeconds)
                .build());

        refreshTokenRepository.delete(optionalRefreshToken);

        return new Token(accessToken, newRefreshToken);
    }

    public boolean verifyToken(String token) throws JwtAuthenticationException {
        try {
            Optional<BlackListToken> optionalBlackListToken = blackListTokenRepository.findById(token);
            if (optionalBlackListToken.isPresent()) {
                throw new JwtAuthenticationException(ErrorCode.BLACKLISTED_ACCESSTOKEN_EXCEPTION);
            }
            Jwts.parserBuilder().setSigningKey(this.getSignKey(secretKey)).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException | IllegalArgumentException e) {
            throw new JwtAuthenticationException(ErrorCode.JWT_BAD_REQUEST);
        } catch (ExpiredJwtException e) {
            throw new JwtAuthenticationException(ErrorCode.JWT_TOKEN_EXPIRED);
        } catch (UnsupportedJwtException e) {
            throw new JwtAuthenticationException(ErrorCode.JWT_UNSUPPORTED_TOKEN);
        }
    }

    public Authentication getAuthentication(String accessToken) {

        SecretKey secret = this.getSignKey(secretKey);

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secret).build()
                .parseClaimsJws(accessToken).getBody();

        Collection<? extends GrantedAuthority> authorities = Arrays.stream(claims.get("auth").toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        UserDetails userDetails = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
    }

    public String resolveToken(HttpServletRequest request) {

        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }

        return null;
    }
}