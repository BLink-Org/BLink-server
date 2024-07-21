package cmc.blink.global.exception;

import cmc.blink.global.exception.constant.ErrorCode;
import lombok.Getter;

import javax.naming.AuthenticationException;

@Getter
public class JwtAuthenticationException extends AuthenticationException {

    private final ErrorCode errorCode;

    public JwtAuthenticationException(ErrorCode code, ErrorCode errorCode) {
        super(code.getMessage());
        this.errorCode = errorCode;
    }

}
