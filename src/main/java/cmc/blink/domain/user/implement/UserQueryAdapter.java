package cmc.blink.domain.user.implement;

import cmc.blink.global.annotation.Adapter;
import cmc.blink.domain.user.persistence.User;
import cmc.blink.domain.user.persistence.UserRepository;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@Adapter
@RequiredArgsConstructor
public class UserQueryAdapter {

    private final UserRepository userRepository;

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
