package cmc.blink.domain.folder.presentation.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class FolderResponse {

    @Getter
    @Builder
    public static class FolderCreateDto {
        Long id;
        String title;
        int sortOrder;
    }

    @Getter
    @Builder
    public static class FolderDto {
        Long id;
        String title;
        int sortOrder;
        int linkCount;
        boolean isRecent;
    }

    @Getter
    @Builder
    public static class FolderListDto {
        int linkTotalCount;
        List<FolderDto> folderDtos;
        int noFolderLinkCount;
    }

}
