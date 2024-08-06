package cmc.blink.domain.user.business;

import cmc.blink.domain.user.persistence.User;
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
}
