package cmc.blink.global.exception;

import cmc.blink.global.exception.constant.ErrorCode;
import lombok.Getter;

import org.springframework.security.core.AuthenticationException;

@Getter
public class JwtAuthenticationException extends AuthenticationException {

    public JwtAuthenticationException(ErrorCode code) {
        super(code.getMessage());
    }
}
