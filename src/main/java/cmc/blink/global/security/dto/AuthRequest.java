package cmc.blink.global.security.dto;

import lombok.Getter;

public class AuthRequest {

    @Getter
    public static class GoogleLoginRequestDto {
        String idToken;
    }

    @Getter
    public static class AppleLoginRequestDto {
        String identityToken;
        String email;
    }

    @Getter
    public static class LogoutRequestDto {
        String refreshToken;
    }

    @Getter
    public static class ReissueRequestDto {
        String refreshToken;
    }
}
