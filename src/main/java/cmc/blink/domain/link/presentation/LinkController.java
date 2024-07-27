package cmc.blink.domain.link.presentation;

import cmc.blink.domain.link.business.LinkService;
import cmc.blink.domain.link.presentation.dto.LinkRequest;
import cmc.blink.domain.link.presentation.dto.LinkResponse;
import cmc.blink.domain.user.persistence.User;
import cmc.blink.global.annotation.AuthUser;
import cmc.blink.global.common.ApiResponseDto;
import cmc.blink.global.exception.dto.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name="링크 API", description = "링크 API 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/links")
public class LinkController {

    private final LinkService linkService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "링크 저장 API", description = "링크 저장 API입니다.")
    @ApiResponses({
            @ApiResponse(description = "<<CREATED>> 링크 저장 완료.", content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "Error Code: 2600", description = "<<BAD_REQUEST>> 입력한 링크 url이 이미 존재함.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "Error Code: 2601", description = "<<BAD_REQUEST>> 유효하지 않은 url.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "Error Code: 2602", description = "<<INTERNAL_SERVER_ERROR>> url 스크랩 과정에서 에러가 발생함.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ApiResponseDto<LinkResponse.LinkCreateDto> saveLink(@Valid @RequestBody final LinkRequest.LinkCreateDto createDto, @AuthUser User user) {
        return ApiResponseDto.created("링크 저장이 완료 되었습니다.", linkService.saveLink(createDto, user));
    }



}

