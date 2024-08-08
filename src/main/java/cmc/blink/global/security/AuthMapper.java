package cmc.blink.global.security;

import cmc.blink.global.security.dto.AuthResponse;
import cmc.blink.global.security.dto.Token;

public class AuthMapper {

    public static AuthResponse.ReissueResponseDto toReissuedTokenResponseDto (Token token) {
        return AuthResponse.ReissueResponseDto.builder()
                .accessToken(token.getAccessToken())
                .refreshToken(token.getRefreshToken())
                .build();
    }

    public static AuthResponse.LoginResponseDto toLoginResponseDto (Token token) {
        return AuthResponse.LoginResponseDto.builder()
                .accessToken(token.getAccessToken())
                .refreshToken(token.getRefreshToken())
                .build();
    }
}
