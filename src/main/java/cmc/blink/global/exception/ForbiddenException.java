package cmc.blink.global.exception;

import cmc.blink.global.exception.constant.ErrorCode;

public class ForbiddenException extends GeneralException {

    public ForbiddenException(ErrorCode errorCode) {
        super(errorCode);
    }

}
