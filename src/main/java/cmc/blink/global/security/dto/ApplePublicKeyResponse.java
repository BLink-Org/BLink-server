package cmc.blink.global.security.dto;

import cmc.blink.global.exception.NotFoundException;
import cmc.blink.global.exception.constant.ErrorCode;
import lombok.Getter;

import java.util.List;

@Getter
public class ApplePublicKeyResponse {

    List<ApplePublicKey> keys;

    public ApplePublicKey getMatchedKey(String kid, String alg){
        return keys.stream()
                .filter(key -> key.getKid().equals(kid) && key.getAlg().equals(alg))
                .findAny()
                .orElseThrow(()-> new NotFoundException(ErrorCode.APPLE_PUBLIC_KEY_NOT_FOUND));
    }
}
