package cmc.blink.domain.user.business;

import cmc.blink.domain.user.implement.UserQueryAdapter;
import cmc.blink.domain.user.persistence.User;
import cmc.blink.domain.user.presentation.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserQueryAdapter userQueryAdapter;

    @Transactional
    public UserResponse.UserInfo findUserEmail(User user) {
        return UserMapper.toUserInfo(user);
    }

}
