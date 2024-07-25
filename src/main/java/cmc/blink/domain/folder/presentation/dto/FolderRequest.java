package cmc.blink.domain.folder.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

public class FolderRequest {

    @Getter
    public static class FolderCreateDto {

        @NotBlank(message = "폴더 제목은 공백으로 설정할 수 없습니다.")
        String title;
    }

}
