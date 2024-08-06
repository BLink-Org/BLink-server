package cmc.blink.global.exception;

import cmc.blink.global.exception.constant.ErrorCode;

public class FeignException extends GeneralException{

    public FeignException(ErrorCode errorCode) {
        super(errorCode);
    }
}
