package cmc.blink.domain.user.presentation;

import cmc.blink.domain.user.business.UserService;
import cmc.blink.domain.user.persistence.User;
import cmc.blink.domain.user.presentation.dto.UserResponse;
import cmc.blink.global.annotation.AuthUser;
import cmc.blink.global.common.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name="유저", description = "마이페이지 관련 API 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "마이페이지 유저 정보 조회 API", description = "마이페이지에서 유저 정보를 조회하는 API입니다.")
    public ApiResponseDto<UserResponse.UserInfo> findUserInfo(@AuthUser User user) {
        return ApiResponseDto.of(userService.findUserInfo(user));
    }


}
