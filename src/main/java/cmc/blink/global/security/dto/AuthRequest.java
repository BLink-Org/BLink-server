package cmc.blink.global.security.dto;

import lombok.Getter;

public class AuthRequest {

    @Getter
    public static class GoogleLoginRequestDto {
        String idToken;
    }

    @Getter
    public static class LogoutRequestDto {
        String refreshToken;
    }
}
