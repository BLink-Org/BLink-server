package cmc.blink.global.security.provider;

import cmc.blink.global.exception.JwtAuthenticationException;
import cmc.blink.global.exception.constant.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SecurityException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtValidator {

    public Map parseHeaders(String token) throws JsonProcessingException {
        String header = token.split("\\.")[0];

        return new ObjectMapper().readValue(decodeHeader(header), Map.class);
    }

    public String decodeHeader(String token) {
        return new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
    }

    public Claims getTokenClaims(String token, PublicKey publicKey) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (SecurityException | MalformedJwtException | IllegalArgumentException e) {
            throw new JwtAuthenticationException(ErrorCode.JWT_BAD_REQUEST);
        } catch (ExpiredJwtException e) {
            throw new JwtAuthenticationException(ErrorCode.JWT_TOKEN_EXPIRED);
        } catch (UnsupportedJwtException e) {
            throw new JwtAuthenticationException(ErrorCode.JWT_UNSUPPORTED_TOKEN);
        }
    }

}
