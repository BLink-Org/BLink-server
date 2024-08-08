package cmc.blink.global.security;

import cmc.blink.global.common.ApiResponseDto;
import cmc.blink.global.security.dto.AuthRequest;
import cmc.blink.global.security.dto.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
}
