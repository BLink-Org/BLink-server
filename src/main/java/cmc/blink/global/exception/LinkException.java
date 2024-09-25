package cmc.blink.global.exception;

import cmc.blink.global.exception.constant.ErrorCode;

public class LinkException extends GeneralException{
    public LinkException(ErrorCode errorCode) {
        super(errorCode);
    }
}
