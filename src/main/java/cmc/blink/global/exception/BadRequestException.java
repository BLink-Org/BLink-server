package cmc.blink.global.exception;

import cmc.blink.global.exception.constant.ErrorCode;

public class BadRequestException extends GeneralException {

    public BadRequestException(ErrorCode errorCode) {
        super(errorCode);
    }
}
