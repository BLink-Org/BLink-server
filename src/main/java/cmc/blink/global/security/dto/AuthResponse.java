package cmc.blink.global.security.dto;

import lombok.Builder;
import lombok.Getter;

public class AuthResponse {

    @Getter
    @Builder
    public static class LoginResponseDto{
        String accessToken;
        String refreshToken;
    }

    @Getter
    @Builder
    public static class ReissueResponseDto{
        String accessToken;
        String refreshToken;
    }
}
