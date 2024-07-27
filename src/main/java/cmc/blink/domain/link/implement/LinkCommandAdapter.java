package cmc.blink.domain.link.implement;

import cmc.blink.domain.link.persistence.Link;
import cmc.blink.domain.link.persistence.LinkRepository;
import cmc.blink.global.annotation.Adapter;
import lombok.RequiredArgsConstructor;

@Adapter
@RequiredArgsConstructor
public class LinkCommandAdapter {

    private final LinkRepository linkRepository;

    public Link create(Link link) {
        return linkRepository.save(link);
    }
}
