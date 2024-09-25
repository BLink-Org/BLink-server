package cmc.blink.domain.link.implement;

import cmc.blink.domain.link.persistence.Link;
import cmc.blink.domain.link.persistence.LinkRepository;
import cmc.blink.domain.link.presentation.dto.LinkRequest;
import cmc.blink.global.annotation.Adapter;
import lombok.RequiredArgsConstructor;

@Adapter
@RequiredArgsConstructor
public class LinkCommandAdapter {

    private final LinkRepository linkRepository;

    public Link create(Link link) {
        return linkRepository.save(link);
    }

    public Link updateTitle(Link link, LinkRequest.LinkTitleUpdateDto updateDto) {
        link.updateTitle(updateDto.getTitle());
        return linkRepository.save(link);
    }

    public Link updateLastViewedAt(Link link) {
        link.updateLastViewedAt();
        return linkRepository.save(link);
    }

    public void updateExcluded(Link link) {
        link.updateExcluded();
        linkRepository.save(link);
    }

    public Link moveToTrash(Link link) {
        link.moveToTrash();
        return linkRepository.save(link);
    }

    public Link recoverFromTrash(Link link) {
        link.recoverFromTrash();
        return linkRepository.save(link);
    }

    public Link toggleLink(Link link) {
        link.togglePin();
        return linkRepository.save(link);
    }

    public void delete(Link link) {
        linkRepository.delete(link);
    }
}
