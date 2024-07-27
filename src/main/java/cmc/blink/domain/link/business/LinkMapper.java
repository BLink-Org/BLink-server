package cmc.blink.domain.link.business;

import cmc.blink.domain.link.persistence.Link;
import cmc.blink.domain.link.presentation.dto.LinkResponse;
import cmc.blink.domain.user.persistence.User;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LinkMapper {

    public static Link toLink(String url, User user, LinkResponse.LinkInfo linkInfo){
        return Link.builder()
                .user(user)
                .url(url)
                .title(linkInfo.getTitle())
                .type(linkInfo.getType())
                .contents(linkInfo.getContents())
                .imageUrl(linkInfo.getImageUrl())
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
}
