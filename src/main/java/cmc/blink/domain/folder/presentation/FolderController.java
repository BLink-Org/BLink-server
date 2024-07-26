package cmc.blink.domain.folder.presentation;

import cmc.blink.domain.folder.business.FolderService;
import cmc.blink.domain.folder.presentation.dto.FolderRequest;
import cmc.blink.domain.folder.presentation.dto.FolderResponse;
import cmc.blink.domain.user.persistence.User;
import cmc.blink.global.annotation.AuthUser;
import cmc.blink.global.common.ApiResponseDto;
import cmc.blink.global.exception.dto.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "폴더", description = "폴더 API입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/folders")
public class FolderController {

    private final FolderService folderService;

    @PatchMapping("/{folderId}")
    @Operation(summary = "폴더 제목 수정 API", description = "폴더 제목 수정 API입니다.")
    @ApiResponses({
            @ApiResponse(description = "<<OK>> 폴더 제목 수정 완료.", content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "Error Code: 1004", description = "<<BAD_REQUEST>> 제목은 공백일 수 없음.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "Error Code: 2400", description = "<<BAD_REQUEST>> 입력한 제목이 이미 존재함.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "Error Code: 2401", description = "<<BAD_REQUEST>> id로 폴더를 찾을 수 없음.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "Error Code: 2402", description = "<<FORBIDDEN>> 해당 폴더의 소유자가 아님.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @Parameters({
            @Parameter(name = "folderId", description = "폴더의 아이디"),
    })
    public ApiResponseDto<?> updateFolderTitle(@Valid @RequestBody final FolderRequest.FolderTitleUpdateDto updateDto,
                                               @PathVariable(name = "folderId") Long folderId ,@AuthUser User user) {

        folderService.updateTitle(updateDto, folderId, user);

        return ApiResponseDto.of("폴더 수정이 완료 되었습니다.", null);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "폴더 생성 API", description = "폴더 생성 API입니다.")
    @ApiResponses({
            @ApiResponse(description = "<<CREATED>> 폴더 생성 완료.", content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "Error Code: 2400", description = "<<BAD_REQUEST>> 입력한 제목이 이미 존재함.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "Error Code: 1004", description = "<<BAD_REQUEST>> 제목은 공백일 수 없음.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ApiResponseDto<FolderResponse.FolderCreateDto> createFolder(@Valid @RequestBody final FolderRequest.FolderCreateDto createDto, @AuthUser User user) {
        return ApiResponseDto.created("폴더 생성이 완료 되었습니다.", folderService.createFolder(createDto, user));
    }



}
