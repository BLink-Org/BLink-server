package cmc.blink.domain.user.business;

import cmc.blink.domain.user.persistence.User;
import cmc.blink.domain.user.presentation.dto.UserResponse;
import cmc.blink.global.security.dto.GoogleUserInfo;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMapper {

    public static User toUser(GoogleUserInfo userInfo, String provider) {
        return User.builder()
                .email(userInfo.getEmail())
                .name(userInfo.getName())
                .provider(provider)
                .build();
    }

    public static UserResponse.UserInfo toUserInfo(User user, boolean deleteRequest, int linkCount, int pinCount, int folderCount) {
        return UserResponse.UserInfo.builder()
                .email(user.getEmail())
                .deleteRequest(deleteRequest)
                .linkCount(linkCount)
                .pinCount(pinCount)
                .folderCount(folderCount)
                .build();
    }
}
