package cmc.blink.domain.folder.implement;

import cmc.blink.domain.folder.persistence.FolderRepository;
import cmc.blink.domain.user.persistence.User;
import cmc.blink.global.annotation.Adapter;
import lombok.RequiredArgsConstructor;

@Adapter
@RequiredArgsConstructor
public class FolderQueryAdapter {

    private final FolderRepository folderRepository;

    public boolean isFolderTitleDuplicate(String title, User user) {
        return folderRepository.existsByUserAndTitle(user, title);
    }

    public int countFolderByUser(User user) {
        return folderRepository.countByUser(user);
    }
}
