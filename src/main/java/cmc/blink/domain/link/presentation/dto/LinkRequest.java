package cmc.blink.domain.link.presentation.dto;

import cmc.blink.global.validator.ByteSize;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

public class LinkRequest {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LinkCreateDto {

        @ByteSize(max = 2500, message = "링크 url은 최대 2000바이트 까지 입력할 수 있습니다.")
        @NotBlank(message = "링크 url은 공백으로 설정할 수 없습니다.")
        String url;

        List<Long> folderIdList;
    }

    @Getter
    public static class LinkToggleDto {

        @ByteSize(max = 2000, message = "링크 url은 최대 2000바이트 까지 입력할 수 있습니다.")
        @NotBlank(message = "링크 url은 공백으로 설정할 수 없습니다.")
        String url;

    }

    @Getter
    public static class LinkTitleUpdateDto {
        @ByteSize(max = 400, message = "링크 제목은 최대 300바이트 까지 입력할 수 있습니다.")
        @NotBlank(message = "링크 제목은 공백으로 설정할 수 없습니다.")
        String title;
    }

    @Getter
    public static class LinkFolderMoveDto {
        List<Long> folderIdList;
    }
}
