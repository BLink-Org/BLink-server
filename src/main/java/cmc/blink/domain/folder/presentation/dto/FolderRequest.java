package cmc.blink.domain.folder.presentation.dto;

import cmc.blink.global.validator.ByteSize;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

public class FolderRequest {

    @Getter
    public static class FolderCreateDto {

        @ByteSize(max = 30, message = "폴더 제목은 최대 30바이트 까지 입력할 수 있습니다.")
        @NotBlank(message = "폴더 제목은 공백으로 설정할 수 없습니다.")
        String title;
    }

    @Getter
    public static class FolderTitleUpdateDto {

        @ByteSize(max = 30, message = "폴더 제목은 최대 30바이트 까지 입력할 수 있습니다.")
        @NotBlank(message = "폴더 제목은 공백으로 설정할 수 없습니다.")
        String title;
    }

}
