package cmc.blink.domain.user.business;

import cmc.blink.domain.user.persistence.User;
import cmc.blink.global.security.OAuth2UserAttributes;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMapper {

    public static User toUser(OAuth2UserAttributes oAuth2UserAttributes) {
        return User.builder()
                .email(oAuth2UserAttributes.getEmail())
                .name(oAuth2UserAttributes.getName())
                .provider(oAuth2UserAttributes.getProvider())
                .build();
    }
}
