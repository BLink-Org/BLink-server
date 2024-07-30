package cmc.blink.domain.folder.business;

import cmc.blink.domain.folder.implement.FolderCommandAdapter;
import cmc.blink.domain.folder.implement.FolderQueryAdapter;
import cmc.blink.domain.folder.persistence.Folder;
import cmc.blink.domain.folder.presentation.dto.FolderRequest;
import cmc.blink.domain.folder.presentation.dto.FolderResponse;
import cmc.blink.domain.link.implement.LinkCommandAdapter;
import cmc.blink.domain.link.implement.LinkFolderCommandAdapter;
import cmc.blink.domain.link.implement.LinkFolderQueryAdapter;
import cmc.blink.domain.link.implement.LinkQueryAdapter;
import cmc.blink.domain.link.persistence.Link;
import cmc.blink.domain.link.persistence.LinkFolder;
import cmc.blink.domain.user.persistence.User;
import cmc.blink.global.exception.FolderException;
import cmc.blink.global.exception.constant.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FolderService {

    private final LinkCommandAdapter linkCommandAdapter;
    private final FolderCommandAdapter folderCommandAdapter;
    private final FolderQueryAdapter folderQueryAdapter;
    private final LinkFolderCommandAdapter linkFolderCommandAdapter;
    private final LinkFolderQueryAdapter linkFolderQueryAdapter;
    private final LinkQueryAdapter linkQueryAdapter;

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

    @Transactional
    public void updateTitle(FolderRequest.FolderTitleUpdateDto updateDto, Long folderId, User user) {

        Folder folder = folderQueryAdapter.findById(folderId);

        if(folder.getUser() != user)
            throw new FolderException(ErrorCode.FOLDER_ACCESS_DENIED);

        if (folderQueryAdapter.isFolderTitleDuplicate(updateDto.getTitle(), user))
            throw new FolderException(ErrorCode.DUPLICATE_FOLDER_TITLE);
        else
            folderCommandAdapter.updateTitle(folder, updateDto);

    }

    @Transactional
    public void deleteFolder(Long folderId, User user) {

        Folder folder = folderQueryAdapter.findById(folderId);

        if(folder.getUser() != user)
            throw new FolderException(ErrorCode.FOLDER_ACCESS_DENIED);

        linkFolderQueryAdapter.findAllByFolder(folder).forEach(linkFolder -> {
            Link link = linkFolder.getLink();
            linkFolderCommandAdapter.delete(linkFolder);

            if (linkFolderQueryAdapter.isOnlyLinkFolder(link)) {
                linkCommandAdapter.moveToTrash(link);
            }
        });

        folderCommandAdapter.delete(folder);
    }
}
