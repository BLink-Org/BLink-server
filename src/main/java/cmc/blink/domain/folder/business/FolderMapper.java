package cmc.blink.domain.folder.business;

import cmc.blink.domain.folder.persistence.Folder;
import cmc.blink.domain.folder.presentation.dto.FolderRequest;
import cmc.blink.domain.folder.presentation.dto.FolderResponse;
import cmc.blink.domain.user.persistence.User;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FolderMapper {

    public static Folder toFolder(FolderRequest.FolderCreateDto createDto, User user, int sortOrder) {
        return Folder.builder()
                .user(user)
                .title(createDto.getTitle())
                .sortOrder(sortOrder)
                .build();
    }

    public static FolderResponse.FolderCreateDto toFolderCreateDto (Folder folder) {
        return FolderResponse.FolderCreateDto.builder()
                .id(folder.getId())
                .title(folder.getTitle())
                .sortOrder(folder.getSortOrder())
                .build();
    }
}
