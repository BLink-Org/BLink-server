package cmc.blink.global.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

public class AuthRequest {

    @Getter
    public static class GoogleLoginRequestDto {

        @Schema(description = "시스템 설정 언어", example = "한국어: KO / 영어: EN")
        String language;

        String idToken;
    }

    @Getter
    public static class AppleLoginRequestDto {

        @Schema(description = "시스템 설정 언어", example = "한국어: KO / 영어: EN")
        String language;

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

    @Getter
    public static class EmailLoginRequestDto {

        @Schema(description = "시스템 설정 언어", example = "한국어: KO / 영어: EN")
        String language;

        String email;
    }
}
