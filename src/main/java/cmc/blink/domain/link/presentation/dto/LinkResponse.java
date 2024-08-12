package cmc.blink.domain.link.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

public class LinkResponse {

    @Getter
    @Builder
    public static class LinkDto {
        Long id;
        String folderName;
        String title;
        String contents;
        LocalDate createdAt;
        String url;
        String imageUrl;
        boolean isPinned;
    }

    @Getter
    @Builder
    public static class LinkListDto {
        int linkCount;
        List<LinkDto> linkDtos;

    }

    @Getter
    @Builder
    public static class LinkCreateDto {
        Long id;
    }

    @Getter
    @Builder
    public static class LinkInfo {
        String title;
        String type;
        String contents;
        String imageUrl;
    }

    @Getter
    @Builder
    public static class FolderIdListDto {
        List<Long> folderIdList;
    }
}
