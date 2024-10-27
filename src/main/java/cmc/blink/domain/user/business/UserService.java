package cmc.blink.domain.user.business;

import cmc.blink.domain.folder.implement.FolderCommandAdapter;
import cmc.blink.domain.folder.implement.FolderQueryAdapter;
import cmc.blink.domain.folder.persistence.Folder;
import cmc.blink.domain.link.implement.LinkCommandAdapter;
import cmc.blink.domain.link.implement.LinkFolderCommandAdapter;
import cmc.blink.domain.link.implement.LinkFolderQueryAdapter;
import cmc.blink.domain.link.implement.LinkQueryAdapter;
import cmc.blink.domain.link.persistence.Link;
import cmc.blink.domain.link.persistence.LinkFolder;
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
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserQueryAdapter userQueryAdapter;
    private final UserCommandAdapter userCommandAdapter;
    private final LinkQueryAdapter linkQueryAdapter;
    private final FolderQueryAdapter folderQueryAdapter;
    private final LinkFolderQueryAdapter linkFolderQueryAdapter;
    private final LinkFolderCommandAdapter linkFolderCommandAdapter;
    private final LinkCommandAdapter linkCommandAdapter;
    private final FolderCommandAdapter folderCommandAdapter;

    @Transactional
    public UserResponse.UserInfo findUserInfo(User user) {

        boolean deleteRequest = user.getDeleteRequestDate() != null;

        LocalDate deleteRequestDate = user.getDeleteRequestDate();

        int linkCount = linkQueryAdapter.countByUserAndIsTrashFalse(user);

        int pinCount = linkQueryAdapter.countPinnedLinksByUserAndIsTrashFalse(user);

        int folderCount = folderQueryAdapter.countFolderByUser(user);

        return UserMapper.toUserInfo(user, deleteRequest, deleteRequestDate, linkCount, pinCount, folderCount);
    }

    @Transactional
    public void applyAccountDeletion(User user) {
        if (user.getDeleteRequestDate()!=null)
            throw new BadRequestException(ErrorCode.USER_ACCOUNT_DELETION_DENIED);

        userCommandAdapter.updateDeleteRequestDate(user);
    }

    @Transactional
    public void cancelAccountDeletion(User user) {
        if (user.getDeleteRequestDate()==null)
            throw new BadRequestException(ErrorCode.USER_ACCOUNT_DELETION_CANCEL_DENIED);

        userCommandAdapter.updateDeleteRequestDate(user);
    }

    @Transactional
    public void deleteExpiredAccounts() {
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
        List<User> usersToDelete = userQueryAdapter.findUsersDeleteRequestBefore(sevenDaysAgo);

        for (User user : usersToDelete) {

            List<Link> links = linkQueryAdapter.findAllByUser(user);
            for (Link link : links) {
                List<LinkFolder> linkFolders = linkFolderQueryAdapter.findAllByLink(link);
                linkFolders.forEach(linkFolderCommandAdapter::delete);
                linkCommandAdapter.delete(link);
            }

            List<Folder> folders = folderQueryAdapter.findAllByUser(user);
            for (Folder folder : folders) {
                List<LinkFolder> linkFolders = linkFolderQueryAdapter.findAllByFolder(folder);
                linkFolders.forEach(linkFolderCommandAdapter::delete);
                folderCommandAdapter.delete(folder);
            }

            userCommandAdapter.delete(user);
        }
    }

    @Transactional
    public void updateLastLoginTime(User user) {
        userCommandAdapter.updateLastLoginTime(user);
    }
}
