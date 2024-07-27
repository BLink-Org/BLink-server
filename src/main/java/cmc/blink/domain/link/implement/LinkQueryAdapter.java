package cmc.blink.domain.link.implement;

import cmc.blink.domain.link.persistence.LinkRepository;
import cmc.blink.domain.user.persistence.User;
import cmc.blink.global.annotation.Adapter;
import lombok.RequiredArgsConstructor;

@Adapter
@RequiredArgsConstructor
public class LinkQueryAdapter {

    private final LinkRepository linkRepository;

    public boolean isLinkUrlDuplicate(String url, User user){
        return linkRepository.existsByUrlAndUser(url, user);
    }

}
