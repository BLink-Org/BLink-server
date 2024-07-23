package cmc.blink.domain.user.implement;

import cmc.blink.global.annotation.Adapter;
import cmc.blink.domain.user.persistence.User;
import cmc.blink.domain.user.persistence.UserRepository;
import lombok.RequiredArgsConstructor;

@Adapter
@RequiredArgsConstructor
public class UserCommandAdapter {

    private final UserRepository userRepository;

    public User save(User user) {
        return userRepository.save(user);
    }

    

}
