package cmc.blink.global.exception;

import cmc.blink.global.exception.constant.ErrorCode;

public class UnauthorizedException extends GeneralException {

    public UnauthorizedException(ErrorCode code) {
        super(code.getMessage());
    }

}
