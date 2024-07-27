package cmc.blink.domain.link.presentation.dto;

import lombok.Builder;
import lombok.Getter;

public class LinkResponse {

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
}
