package cmc.blink.domain.folder.presentation.dto;

import lombok.Builder;
import lombok.Getter;

public class FolderResponse {

    @Getter
    @Builder
    public static class FolderCreateDto {
        Long id;
        String title;
        int sortOrder;
    }

}
