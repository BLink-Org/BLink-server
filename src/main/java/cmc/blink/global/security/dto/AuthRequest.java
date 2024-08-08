package cmc.blink.global.security.dto;

import lombok.Getter;

public class AuthRequest {

    @Getter
    public static class GoogleLoginRequestDto {
        String idToken;
    }
}
