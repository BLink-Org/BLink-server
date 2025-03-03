package cmc.blink.global.security;

import cmc.blink.domain.user.persistence.User;
import cmc.blink.global.annotation.AuthUser;
import cmc.blink.global.common.ApiResponseDto;
import cmc.blink.global.security.dto.AuthRequest;
import cmc.blink.global.security.dto.AuthResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

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

    @PostMapping("/login/apple")
    @Operation(summary = "애플 로그인 API", description = "애플 로그인 API입니다.")
    public ApiResponseDto<AuthResponse.LoginResponseDto> appleLogin(@RequestBody AuthRequest.AppleLoginRequestDto requestDto) throws NoSuchAlgorithmException, InvalidKeySpecException, JsonProcessingException {
        return ApiResponseDto.of(authService.appleLogin(requestDto));
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃 API", description = "로그아웃 API입니다.")
    public ApiResponseDto<?> logout(HttpServletRequest httpServletRequest, @RequestBody AuthRequest.LogoutRequestDto logoutRequestDto, @AuthUser User user) {
        authService.logout(httpServletRequest, logoutRequestDto, user);
        return ApiResponseDto.of("로그아웃이 완료되었습니다.", null);
    }

    @PostMapping("/reissue")
    @Operation(summary = "토큰 재발급 API", description = "토큰 재발급 API입니다.")
    public ApiResponseDto<AuthResponse.ReissueResponseDto>reissue(@RequestBody AuthRequest.ReissueRequestDto reissueRequestDto){

        return ApiResponseDto.of("토큰 재발급이 완료되었습니다.", authService.reissue(reissueRequestDto));
    }

    @PostMapping("/login/email")
    @Operation(summary = "크롬 익스텐션 로그인 API", description = "크롬 익스텐션용 로그인 API입니다.")
public ApiResponseDto<AuthResponse.LoginResponseDto> login(@RequestBody AuthRequest.EmailLoginRequestDto requestDto) {
        return ApiResponseDto.of(authService.chromeExtensionLogin(requestDto));
    }
}
