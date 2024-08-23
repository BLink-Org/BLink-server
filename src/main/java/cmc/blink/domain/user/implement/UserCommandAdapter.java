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

    public User updateDeleteRequestDate(User user) {
        user.updateDeleteRequestDate();

        return userRepository.save(user);
    }

    public void delete(User user) {
        userRepository.delete(user);
    }

}
