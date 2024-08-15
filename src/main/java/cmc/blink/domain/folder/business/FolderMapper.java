package cmc.blink.domain.folder.business;

import cmc.blink.domain.folder.persistence.Folder;
import cmc.blink.domain.folder.presentation.dto.FolderRequest;
import cmc.blink.domain.folder.presentation.dto.FolderResponse;
import cmc.blink.domain.user.persistence.User;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FolderMapper {

    public static Folder toFolder(FolderRequest.FolderCreateDto createDto, User user, int sortOrder) {
        return Folder.builder()
                .user(user)
                .title(createDto.getTitle())
                .sortOrder(sortOrder)
                .build();
    }

    public static Folder toFolder(String title, User user, int sortOrder) {
        return Folder.builder()
                .user(user)
                .title(title)
                .sortOrder(sortOrder)
                .build();
    }

    public static FolderResponse.FolderDto toFolderDto(Folder folder, int linkCount, boolean isRecent){
        return FolderResponse.FolderDto.builder()
                .id(folder.getId())
                .title(folder.getTitle())
                .sortOrder(folder.getSortOrder())
                .linkCount(linkCount)
                .isRecent(isRecent)
                .build();
    }

    public static FolderResponse.FolderListDto toFolderListDto (List<FolderResponse.FolderDto> folderList, int linkTotalCount, int noFolderLinkCount) {
        return FolderResponse.FolderListDto.builder()
                .linkTotalCount(linkTotalCount)
                .folderDtos(folderList)
                .noFolderLinkCount(noFolderLinkCount)
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
