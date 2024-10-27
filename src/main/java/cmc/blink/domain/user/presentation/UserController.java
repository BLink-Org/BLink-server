package cmc.blink.domain.user.presentation;

import cmc.blink.domain.user.business.UserService;
import cmc.blink.domain.user.persistence.User;
import cmc.blink.domain.user.presentation.dto.UserResponse;
import cmc.blink.global.annotation.AuthUser;
import cmc.blink.global.common.ApiResponseDto;
import cmc.blink.global.exception.dto.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    @PatchMapping("/delete")
    @Operation(summary = "계정 삭제 신청 API", description = "계정 삭제 신청 API입니다.")
    @ApiResponses({
            @ApiResponse(description = "<<OK>> 계정 삭제 신청 완료.", content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "Error Code: 2202", description = "<<BAD_REQUEST>> 이미 삭제 신청을 한 사용자입니다.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ApiResponseDto<?> applyAccountDeletion(@AuthUser User user) {
        userService.applyAccountDeletion(user);

        return ApiResponseDto.of("계정 삭제 신청이 완료되었습니다.", null);
    }

    @PatchMapping("/cancel")
    @Operation(summary = "계정 삭제 철회 API", description = "계정 삭제 철회 API입니다.")
    @ApiResponses({
            @ApiResponse(description = "<<OK>> 계정 삭제 철회 완료.", content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "Error Code: 2203", description = "<<BAD_REQUEST>> 삭제를 신청한 사용자만 삭제 신청을 취소할 수 있습니다.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ApiResponseDto<?> cancelAccountDeletion(@AuthUser User user) {
        userService.cancelAccountDeletion(user);

        return ApiResponseDto.of("계정 삭제 신청 취소가 완료되었습니다.", null);
    }

    @PatchMapping("/last-access")
    @Operation(summary = "최근 접속 일시 업데이트 API", description = "사용자가 앱에 마지막으로 접속한 일시를 업데이트하는 API입니다.")
    @ApiResponses({
            @ApiResponse(description = "<<OK>> 접속 일시 업데이트 완료.", content = @Content(schema = @Schema(implementation = ApiResponseDto.class)))})
    public ApiResponseDto<?> updateLastLoginTime(@AuthUser User user) {
        userService.updateLastLoginTime(user);

        return ApiResponseDto.of("최근 접속 일시 업데이트가 완료되었습니다.", null);
    }

}
