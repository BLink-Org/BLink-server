package cmc.blink.global.exception;

import cmc.blink.global.exception.constant.ErrorCode;

public class OAuth2Exception extends GeneralException{

    public OAuth2Exception(ErrorCode code) {
        super(code.getMessage());
    }
}
