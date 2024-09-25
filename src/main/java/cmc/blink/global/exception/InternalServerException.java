package cmc.blink.global.exception;

import cmc.blink.global.exception.constant.ErrorCode;

public class InternalServerException extends GeneralException {

    public InternalServerException(ErrorCode errorCode) {
        super(errorCode);
    }

}
