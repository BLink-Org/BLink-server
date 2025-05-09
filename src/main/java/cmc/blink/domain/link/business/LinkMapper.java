package cmc.blink.domain.link.business;

import cmc.blink.domain.link.persistence.Link;
import cmc.blink.domain.link.persistence.LinkFolder;
import cmc.blink.domain.link.presentation.dto.LinkResponse;
import cmc.blink.domain.user.persistence.User;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LinkMapper {

    public static Link toLink(String url, User user, LinkResponse.LinkInfo linkInfo){
        return Link.builder()
                .user(user)
                .url(url)
                .title(Optional.ofNullable(linkInfo.getTitle()).orElse(""))
                .type(Optional.ofNullable(linkInfo.getType()).orElse(""))
                .contents(Optional.ofNullable(linkInfo.getContents()).orElse(""))
                .imageUrl(Optional.ofNullable(linkInfo.getImageUrl()).orElse(""))
                .build();
    }

    public static LinkResponse.LinkDto toLinkDto(Link link, String folderName) {
        return LinkResponse.LinkDto.builder()
                .id(link.getId())
                .folderName(folderName)
                .title(link.getTitle())
                .contents(link.getContents())
                .createdAt(LocalDate.from(link.getCreatedAt()))
                .url(link.getUrl())
                .imageUrl(link.getImageUrl())
                .isPinned(link.isPinned())
                .build();
    }

    public static LinkResponse.LinkListDto toLinkListDto(List<LinkResponse.LinkDto> linkDtos, int linkCount) {
        return LinkResponse.LinkListDto.builder()
                .linkCount(linkCount)
                .linkDtos(linkDtos)
                .build();
    }

    public static LinkResponse.LinkCreateDto toLinkCreateDto(Link link) {
        return  LinkResponse.LinkCreateDto.builder()
                .id(link.getId())
                .build();
    }

    public static LinkResponse.LinkInfo toLinkInfo(String title, String type, String contents, String imageUrl) {
        return LinkResponse.LinkInfo.builder()
                .title(title)
                .type(type)
                .contents(contents)
                .imageUrl(imageUrl)
                .build();
    }

    public static LinkResponse.FolderIdListDto toFolderIdListDto(List<LinkFolder> linkFolders) {

        return LinkResponse.FolderIdListDto.builder()
                .folderIdList(linkFolders.stream()
                        .map(linkFolder -> linkFolder.getFolder().getId())
                        .collect(Collectors.toList()))
                .build();
    }

    public static LinkResponse.LastViewedLinkListDto toLastViewedLinkListDto(List<LinkResponse.LinkDto> linkDtos) {
        return LinkResponse.LastViewedLinkListDto.builder()
                .linkDtos(linkDtos)
                .build();
    }
}
