package cmc.blink.domain.user.business;

import cmc.blink.domain.user.persistence.User;
import cmc.blink.domain.user.presentation.dto.UserResponse;
import cmc.blink.global.security.dto.AuthRequest;
import cmc.blink.global.security.dto.GoogleUserInfo;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMapper {

    public static User toUser(GoogleUserInfo userInfo, String provider) {
        return User.builder()
                .email(userInfo.getEmail())
                .name(userInfo.getName())
                .provider(provider)
                .build();
    }

    public static User toUser(AuthRequest.AppleLoginRequestDto requestDto, String provider) {
        return User.builder()
                .email(requestDto.getEmail())
                .provider(provider)
                .build();
    }

    public static UserResponse.UserInfo toUserInfo(User user, boolean deleteRequest, LocalDate deleteRequestDate, int linkCount, int pinCount, int folderCount) {
        return UserResponse.UserInfo.builder()
                .email(user.getEmail())
                .deleteRequest(deleteRequest)
                .deleteRequestDate(deleteRequestDate)
                .linkCount(linkCount)
                .pinCount(pinCount)
                .folderCount(folderCount)
                .build();
    }
}
