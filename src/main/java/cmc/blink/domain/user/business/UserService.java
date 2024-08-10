package cmc.blink.domain.user.business;

import cmc.blink.domain.folder.implement.FolderQueryAdapter;
import cmc.blink.domain.link.implement.LinkQueryAdapter;
import cmc.blink.domain.user.implement.UserQueryAdapter;
import cmc.blink.domain.user.persistence.Status;
import cmc.blink.domain.user.persistence.User;
import cmc.blink.domain.user.presentation.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserQueryAdapter userQueryAdapter;
    private final LinkQueryAdapter linkQueryAdapter;
    private final FolderQueryAdapter folderQueryAdapter;

    @Transactional
    public UserResponse.UserInfo findUserInfo(User user) {

        boolean deleteRequest = user.getDeleteRequestDate() != null;

        int linkCount = linkQueryAdapter.countByUserAndIsTrashFalse(user);

        int pinCount = linkQueryAdapter.countPinnedLinksByUserAndIsTrashFalse(user);

        int folderCount = folderQueryAdapter.countFolderByUser(user);

        return UserMapper.toUserInfo(user, deleteRequest, linkCount, pinCount, folderCount);
    }

}
