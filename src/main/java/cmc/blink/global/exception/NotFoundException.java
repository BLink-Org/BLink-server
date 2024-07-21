package cmc.blink.global.exception;

import cmc.blink.global.exception.constant.ErrorCode;

public class NotFoundException extends GeneralException {

    public NotFoundException(ErrorCode code) {
        super(code.getMessage());
    }

}
