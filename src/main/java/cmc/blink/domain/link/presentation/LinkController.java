package cmc.blink.domain.link.presentation;

import cmc.blink.domain.link.business.LinkService;
import cmc.blink.domain.link.presentation.dto.LinkRequest;
import cmc.blink.domain.link.presentation.dto.LinkResponse;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name="링크", description = "링크 API 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/links")
public class LinkController {

    private final LinkService linkService;

    @GetMapping
    @Operation(summary = "링크 목록 조회 API", description = "링크 목록 조회 API입니다.")
    @Parameters({
            @Parameter(name = "folderId", description = """
                    폴더의 아이디
                    
                    홈 - 전체 링크 조회 : folderId 없이 /
                    
                    각 폴더에 저장된 링크 조회 : 해당 folderId
                    
                    폴더 없는 링크 조회: folderId = 0 으로

                    담아서 요청 주시면 됩니다 ❗❗"""),
            @Parameter(name = "sortBy", description = "<정렬 기준>\n최근 저장순: createdAt_desc / 과거 저장순: createdAt_asc / 제목순(A-ㅎ): title_asc / 제목순(ㅎ-A): title_desc"),
            @Parameter(name = "page", description = "페이지 번호, ❗❗ 첫 페이지는 0번 입니다 ❗❗"),
            @Parameter(name = "size", description = "페이징 사이즈")
    })
    public ApiResponseDto<LinkResponse.LinkListDto> findAllLinks (@RequestParam(required = false, name = "folderId")Long folderId,
                                                                  @RequestParam(defaultValue = "createdAt_desc", name = "sortBy") String sortBy,
                                                                  @RequestParam(defaultValue = "0", name = "page") int page,
                                                                  @RequestParam(defaultValue = "10", name = "size") int size,
                                                                  @AuthUser User user) {
        LinkResponse.LinkListDto linkListDto;

        String[] sortParams = sortBy.split("_");
        String field = sortParams[0];
        Sort.Direction direction = Sort.Direction.fromString(sortParams[1]);

        Pageable pageable = PageRequest.of(page, size, direction, field);

        if(folderId == null)
            linkListDto = linkService.findLinkPaging(user, pageable);
        else if (folderId != 0)
            linkListDto = linkService.findLinkFolderPaging(user, folderId, pageable);
        else
            linkListDto = linkService.findNoFolderLinkPaging(user, pageable);

        return ApiResponseDto.of(linkListDto);
    }

    @GetMapping("/pinned")
    @Operation(summary = "핀 고정 링크 목록 조회 API", description = "핀 고정 링크 목록 조회 API입니다.")
    @Parameters({
            @Parameter(name = "sortBy", description = "<정렬 기준>\n최근 핀 추가순: pinnedAt_desc / 과거 핀 추가순: pinnedAt_asc"),
            @Parameter(name = "page", description = "페이지 번호, ❗❗ 첫 페이지는 0번 입니다 ❗❗"),
            @Parameter(name = "size", description = "페이징 사이즈")
    })
    public ApiResponseDto<LinkResponse.LinkListDto> findPinnedLinks(@RequestParam(defaultValue = "pinnedAt_desc", name = "sortBy") String sortBy,
                                                                    @RequestParam(defaultValue = "0", name = "page") int page,
                                                                    @RequestParam(defaultValue = "10", name = "size") int size,
                                                                    @AuthUser User user) {
        String[] sortParams = sortBy.split("_");
        String field = sortParams[0];
        Sort.Direction direction = Sort.Direction.fromString(sortParams[1]);

        Pageable pageable = PageRequest.of(page, size, direction, field);

        return ApiResponseDto.of(linkService.findPinnedLinks(user, pageable));
    }

    @GetMapping("/trash")
    @Operation(summary = "휴지통 링크 목록 조회 API", description = "휴지통 링크 목록 조회 API입니다.")
    @Parameters({
            @Parameter(name = "sortBy", description = "<정렬 기준>\n최근 삭제 순: trashMovedDate_desc / 과거 삭제 순: trashMovedDate_asc"),
            @Parameter(name = "page", description = "페이지 번호, ❗❗ 첫 페이지는 0번 입니다 ❗❗"),
            @Parameter(name = "size", description = "페이징 사이즈")
    })
    public ApiResponseDto<LinkResponse.LinkListDto> findTrashLinks(@RequestParam(defaultValue = "trashMovedDate_desc", name = "sortBy") String sortBy,
                                                                   @RequestParam(defaultValue = "0", name = "page") int page,
                                                                   @RequestParam(defaultValue = "10", name = "size") int size,
                                                                    @AuthUser User user) {

        String[] sortParams = sortBy.split("_");
        String field = sortParams[0];
        Sort.Direction direction = Sort.Direction.fromString(sortParams[1]);

        Pageable pageable = PageRequest.of(page, size, direction, field);

        return ApiResponseDto.of(linkService.findTrashLinks(user, pageable));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "링크 저장 API", description = "링크 저장 API입니다.")
    @ApiResponses({
            @ApiResponse(description = "<<CREATED>> 링크 저장 완료.", content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "Error Code: 1004", description = "<<BAD_REQUEST>> 링크 url은 공백일 수 없음. / 링크 url은 최대 2000바이트 까지 입력 제한", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "Error Code: 2600", description = "<<BAD_REQUEST>> 입력한 링크 url이 이미 존재함.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "Error Code: 2601", description = "<<BAD_REQUEST>> 유효하지 않은 url.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "Error Code: 2602", description = "<<INTERNAL_SERVER_ERROR>> url 스크랩 과정에서 에러가 발생함.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ApiResponseDto<LinkResponse.LinkCreateDto> saveLink(@Valid @RequestBody final LinkRequest.LinkCreateDto createDto, @AuthUser User user) {
        return ApiResponseDto.created("링크 저장이 완료 되었습니다.", linkService.saveLink(createDto, user));
    }

    @PatchMapping("/{linkId}")
    @Operation(summary = "링크 제목 수정 API", description = "링크 제목 수정 API입니다.")
    @ApiResponses({
            @ApiResponse(description = "<<OK>> 링크 제목 수정 완료.", content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "Error Code: 1004", description = "<<BAD_REQUEST>> 링크 제목은 공백일 수 없음. /링크 제목은 최대 300바이트 까지 입력 제한", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "Error Code: 2603", description = "<<BAD_REQUEST>> id로 링크를 찾을 수 없음.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "Error Code: 2604", description = "<<FORBIDDEN>> 해당 링크의 소유자가 아님.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @Parameters({
            @Parameter(name = "linkId", description = "링크의 아이디")
    })
    public ApiResponseDto<?> updateLinkTitle(@Valid @RequestBody LinkRequest.LinkTitleUpdateDto updateDto,
                                             @PathVariable(name = "linkId") Long linkId, @AuthUser User user) {
        linkService.updateTitle(updateDto, linkId, user);

        return ApiResponseDto.of("링크 제목 수정이 완료 되었습니다.", null);
    }

    @PostMapping("/{linkId}/move")
    @Operation(summary = "링크 저장 폴더 변경 API", description = "링크 저장 폴더 변경 API입니다.\n" +
            "폴더 없이 저장 선택 시 folderIdList 비워서 보내주시면 됩니다~~")
    @ApiResponses({
            @ApiResponse(description = "<<OK>> 링크 저장 폴더 수정 완료.", content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "Error Code: 2603", description = "<<BAD_REQUEST>> id로 링크를 찾을 수 없음.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "Error Code: 2604", description = "<<FORBIDDEN>> 해당 링크의 소유자가 아님.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @Parameters({
            @Parameter(name = "linkId", description = "링크의 아이디")
    })
    public ApiResponseDto<?> moveLink(@PathVariable(name = "linkId") Long linkId
            , @RequestBody LinkRequest.LinkFolderMoveDto updateDto, @AuthUser User user) {

        linkService.moveLink(linkId, updateDto, user);

        return ApiResponseDto.of("링크 저장 폴더 수정이 완료 되었습니다.", null);
    }

    @PatchMapping("/{linkId}/view")
    @Operation(summary = "링크 조회 API", description = "특정 링크를 읽을 때 해당 링크의 조회일시를 업데이트하기 위한 API입니다.")
    @ApiResponses({
            @ApiResponse(description = "<<OK>> 링크 조회일시 업데이트 완료.", content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "Error Code: 2603", description = "<<BAD_REQUEST>> id로 링크를 찾을 수 없음.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "Error Code: 2604", description = "<<FORBIDDEN>> 해당 링크의 소유자가 아님.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @Parameters({
            @Parameter(name = "linkId", description = "링크의 아이디")
    })
    public ApiResponseDto<?> updateLinkLastViewedAt(@PathVariable(name = "linkId") Long linkId, @AuthUser User user){
        linkService.updateLastViewedAt(linkId, user);

        return ApiResponseDto.of("링크 조회일시 업데이트가 완료되었습니다.", null);
    }


    @PatchMapping("/{linkId}/trash/move")
    @Operation(summary = "링크 휴지통 이동 API", description = "링크를 휴지통으로 이동하는 API입니다.")
    @ApiResponses({
            @ApiResponse(description = "<<OK>> 링크 휴지통 이동 완료.", content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "Error Code: 2603", description = "<<BAD_REQUEST>> id로 링크를 찾을 수 없음.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "Error Code: 2604", description = "<<FORBIDDEN>> 해당 링크의 소유자가 아님.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @Parameters({
            @Parameter(name = "linkId", description = "링크의 아이디")
    })
    public ApiResponseDto<?> moveLinkToTrash(@PathVariable(name = "linkId") Long linkId, @AuthUser User user) {

        linkService.moveLinkToTrash(linkId, user);

        return ApiResponseDto.of("휴지통으로 이동이 완료 되었습니다.", null);
    }

    @PatchMapping("/{linkId}/trash/recover")
    @Operation(summary = "휴지통에서 링크 복구 API", description = "휴지통에서 링크를 다시 복구하는 API입니다.")
    @ApiResponses({
            @ApiResponse(description = "<<OK>> 링크 휴지통 이동 완료.", content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "Error Code: 2603", description = "<<BAD_REQUEST>> id로 링크를 찾을 수 없음.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "Error Code: 2604", description = "<<FORBIDDEN>> 해당 링크의 소유자가 아님.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @Parameters({
            @Parameter(name = "linkId", description = "링크의 아이디")
    })
    public ApiResponseDto<?> recoverLinkFromTrash(@PathVariable(name = "linkId") Long linkId, @AuthUser User user) {

        linkService.recoverLinkFromTrash(linkId, user);

        return ApiResponseDto.of("휴지통에서 복구가 완료 되었습니다.", null);
    }

    @PatchMapping("/{linkId}/pin/toggle")
    @Operation(summary = "링크 고정 토글 API", description = "링크 고정/ 고정 취소 토글 API입니다.")
    @ApiResponses({
            @ApiResponse(description = "<<OK>> 링크 고정 토글 완료.", content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "Error Code: 2603", description = "<<BAD_REQUEST>> id로 링크를 찾을 수 없음.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "Error Code: 2604", description = "<<FORBIDDEN>> 해당 링크의 소유자가 아님.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @Parameters({
            @Parameter(name = "linkId", description = "링크의 아이디")
    })
    public ApiResponseDto<?> toggleLink(@PathVariable(name = "linkId") Long linkId, @AuthUser User user) {

        linkService.toggleLink(linkId, user);

        return ApiResponseDto.of("링크 고정 토글이 완료 되었습니다.", null);
    }

    @DeleteMapping("/{linkId}")
    @Operation(summary = "휴지통에서 링크 영구삭제 API", description = "휴지통에서 링크를 영구삭제하는 API입니다.")
    @ApiResponses({
            @ApiResponse(description = "<<OK>> 링크 영구삭제 완료.", content = @Content(schema = @Schema(implementation = ApiResponseDto.class))),
            @ApiResponse(responseCode = "Error Code: 2603", description = "<<BAD_REQUEST>> id로 링크를 찾을 수 없음.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "Error Code: 2604", description = "<<FORBIDDEN>> 해당 링크의 소유자가 아님.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "Error Code: 2605", description = "<<BAD_REQUEST>> 휴지통에 있는 링크만 영구삭제 가능.", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))

    })
    @Parameters({
            @Parameter(name = "linkId", description = "링크의 아이디")
    })
    public ApiResponseDto<?> deleteLink(@PathVariable(name = "linkId") Long linkId, @AuthUser User user) {

        linkService.deleteLink(linkId, user);

        return ApiResponseDto.of("링크 영구삭제가 완료 되었습니다.", null);
    }

}

