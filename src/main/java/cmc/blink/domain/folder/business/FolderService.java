package cmc.blink.domain.folder.business;

import cmc.blink.domain.folder.implement.FolderCommandAdapter;
import cmc.blink.domain.folder.implement.FolderQueryAdapter;
import cmc.blink.domain.folder.persistence.Folder;
import cmc.blink.domain.folder.presentation.dto.FolderRequest;
import cmc.blink.domain.folder.presentation.dto.FolderResponse;
import cmc.blink.domain.user.persistence.User;
import cmc.blink.global.exception.FolderException;
import cmc.blink.global.exception.constant.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FolderService {

    private final FolderCommandAdapter folderCommandAdapter;
    private final FolderQueryAdapter folderQueryAdapter;

    @Transactional
    public FolderResponse.FolderCreateDto createFolder(FolderRequest.FolderCreateDto createDto, User user) {

        if (folderQueryAdapter.isFolderTitleDuplicate(createDto.getTitle(), user)) {
            throw new FolderException(ErrorCode.DUPLICATE_FOLDER_TITLE);
        } else {

            int folderCount = folderQueryAdapter.countFolderByUser(user);
            int sortOrder = folderCount + 1;

            Folder folder = FolderMapper.toFolder(createDto, user, sortOrder);

            return FolderMapper.toFolderCreateDto(folderCommandAdapter.create(folder));
        }
    }

}
