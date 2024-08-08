package cmc.blink.global.security;

import cmc.blink.domain.user.persistence.User;
import cmc.blink.global.annotation.AuthUser;
import cmc.blink.global.common.ApiResponseDto;
import cmc.blink.global.security.dto.AuthRequest;
import cmc.blink.global.security.dto.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name="인증", description = "인증 관련 API 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login/google")
    @Operation(summary = "구글 로그인 API", description = "구글 로그인 API입니다.")
    public ApiResponseDto<AuthResponse.LoginResponseDto> googleLogin(@RequestBody AuthRequest.GoogleLoginRequestDto requestDto){
        return ApiResponseDto.of(authService.googleLogin(requestDto));
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃 API", description = "로그아웃 API입니다.")
    public ApiResponseDto<?> logout(HttpServletRequest httpServletRequest, AuthRequest.LogoutRequestDto logoutRequestDto, @AuthUser User user) {
        authService.logout(httpServletRequest, logoutRequestDto, user);
        return ApiResponseDto.of("로그아웃이 완료되었습니다.", null);
    }

    @PostMapping("/reissue")
    @Operation(summary = "토큰 재발급 API", description = "토큰 재발급 API입니다.")
    public ApiResponseDto<AuthResponse.ReissueResponseDto>reissue(AuthRequest.ReissueRequestDto reissueRequestDto){

        return ApiResponseDto.of("토큰 재발급이 완료되었습니다.", authService.reissue(reissueRequestDto));
    }
}
