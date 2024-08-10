package cmc.blink.domain.user.business;

import cmc.blink.domain.folder.implement.FolderQueryAdapter;
import cmc.blink.domain.link.implement.LinkQueryAdapter;
import cmc.blink.domain.user.implement.UserCommandAdapter;
import cmc.blink.domain.user.implement.UserQueryAdapter;
import cmc.blink.domain.user.persistence.Status;
import cmc.blink.domain.user.persistence.User;
import cmc.blink.domain.user.presentation.dto.UserResponse;
import cmc.blink.global.exception.BadRequestException;
import cmc.blink.global.exception.constant.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserQueryAdapter userQueryAdapter;
    private final UserCommandAdapter userCommandAdapter;
    private final LinkQueryAdapter linkQueryAdapter;
    private final FolderQueryAdapter folderQueryAdapter;

    @Transactional
    public UserResponse.UserInfo findUserInfo(User user) {

        boolean deleteRequest = user.getDeleteRequestDate() != null;

        LocalDate deleteRequestDate = user.getDeleteRequestDate();

        int linkCount = linkQueryAdapter.countByUserAndIsTrashFalse(user);

        int pinCount = linkQueryAdapter.countPinnedLinksByUserAndIsTrashFalse(user);

        int folderCount = folderQueryAdapter.countFolderByUser(user);

        return UserMapper.toUserInfo(user, deleteRequest, deleteRequestDate, linkCount, pinCount, folderCount);
    }

    public void applyAccountDeletion(User user) {
        if (user.getDeleteRequestDate()!=null)
            throw new BadRequestException(ErrorCode.USER_ACCOUNT_DELETION_DENIED);

        userCommandAdapter.updateDeleteRequestDate(user);
    }

    public void cancelAccountDeletion(User user) {
        if (user.getDeleteRequestDate()==null)
            throw new BadRequestException(ErrorCode.USER_ACCOUNT_DELETION_CANCEL_DENIED);

        userCommandAdapter.updateDeleteRequestDate(user);
    }
}
