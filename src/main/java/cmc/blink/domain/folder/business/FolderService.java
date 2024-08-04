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

import java.util.ArrayList;
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
    public FolderResponse.FolderListDto findFolders(User user) {

        int linkTotalCount = linkQueryAdapter.countByUser(user);

        // findByUser로 유저의 총 Folder를 찾고, 각 Folder 마다 findAllByUserAndFolder로 LinkFolder 개수 찾아서 FolderMapper의 toFolderDto
        List<Folder> folderList = folderQueryAdapter.findAllByUserOrderBySortOrderAsc(user);
        List<FolderResponse.FolderDto> folderDtos = new ArrayList<>();

        Folder recentFolder = folderQueryAdapter.findLastLinkedFolder(user);

        Long recentFolderId = recentFolder != null ? recentFolder.getId() : null;

        for (Folder folder: folderList) {
            int linkCount = linkFolderQueryAdapter.countByFolder(folder);
            boolean isRecent = folder.getId().equals(recentFolderId);
            FolderResponse.FolderDto folderDto = FolderMapper.toFolderDto(folder, linkCount, isRecent);
            folderDtos.add(folderDto);
        }

        int noFolderLinkCount = linkQueryAdapter.countByUserAndNoFolder(user);

        return FolderMapper.toFolderListDto(folderDtos, linkTotalCount, noFolderLinkCount);
    }

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
    public void moveFolderUp(Long folderId, User user){
        List<Folder> folders = folderQueryAdapter.findAllByUserOrderBySortOrderAsc(user);

        for (int i = 0; i < folders.size(); i++) {
            if (folders.get(i).getId().equals(folderId) && i > 0) {

                Folder currentFolder = folders.get(i);
                Folder previousFolder = folders.get(i - 1);

                folderCommandAdapter.swapSortOrder(currentFolder, previousFolder);

                break;
            }
        }
    }

    @Transactional
    public void moveFolderDown(Long folderId, User user) {
        List<Folder> folders = folderQueryAdapter.findAllByUserOrderBySortOrderAsc(user);

        for (int i = 0; i < folders.size(); i++) {
            if (folders.get(i).getId().equals(folderId) && i < folders.size() - 1) {

                Folder currentFolder = folders.get(i);
                Folder nextFolder = folders.get(i + 1);

                folderCommandAdapter.swapSortOrder(currentFolder, nextFolder);

                break;
            }
        }


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
        folderCommandAdapter.reassignSortOrders(user);
    }
}
