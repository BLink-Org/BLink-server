package cmc.blink.domain.folder.implement;

import cmc.blink.domain.folder.persistence.Folder;
import cmc.blink.domain.folder.persistence.FolderRepository;
import cmc.blink.domain.user.persistence.User;
import cmc.blink.global.annotation.Adapter;
import cmc.blink.global.exception.FolderException;
import cmc.blink.global.exception.constant.ErrorCode;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Adapter
@RequiredArgsConstructor
public class FolderQueryAdapter {

    private final FolderRepository folderRepository;

    public Folder findById(Long folderId) {
        return folderRepository.findById(folderId).orElseThrow(
                () -> new FolderException(ErrorCode.FOLDER_NOT_FOUND));
    }

    public List<Folder> findAllById(List<Long> folderIds) {
        return folderRepository.findAllById(folderIds);
    }

    public boolean isFolderTitleDuplicate(String title, User user) {
        return folderRepository.existsByUserAndTitle(user, title);
    }

    public List<Folder> findAllByUserOrderBySortOrderAsc(User user) {
        return folderRepository.findAllByUserOrderBySortOrderAsc(user);
    }

    public int countFolderByUser(User user) {
        return folderRepository.countByUser(user);
    }

    public Folder findLastLinkedFolder(User user) {
        List<Folder> recentFolders = folderRepository.findTop1ByUserOrderByLastLinkedAtDesc(user);
        return recentFolders.isEmpty() ? null : recentFolders.get(0);
    }

    public List<Folder> findAllByUser(User user) {
        return folderRepository.findAllByUser(user);
    }
}
