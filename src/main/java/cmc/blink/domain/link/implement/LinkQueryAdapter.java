package cmc.blink.domain.link.implement;

import cmc.blink.domain.link.persistence.Link;
import cmc.blink.domain.link.persistence.LinkRepository;
import cmc.blink.domain.user.persistence.User;
import cmc.blink.global.annotation.Adapter;
import cmc.blink.global.exception.LinkException;
import cmc.blink.global.exception.constant.ErrorCode;
import lombok.RequiredArgsConstructor;

@Adapter
@RequiredArgsConstructor
public class LinkQueryAdapter {

    private final LinkRepository linkRepository;

    public boolean isLinkUrlDuplicate(String url, User user){
        return linkRepository.existsByUrlAndUser(url, user);
    }

    public Link findById(Long id) {
        return linkRepository.findById(id).orElseThrow(()-> new LinkException(ErrorCode.LINK_NOT_FOUND));
    }

}
