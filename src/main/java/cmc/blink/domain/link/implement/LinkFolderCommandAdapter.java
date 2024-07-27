package cmc.blink.domain.link.implement;

import cmc.blink.domain.link.persistence.LinkFolder;
import cmc.blink.domain.link.persistence.LinkFolderRepository;
import cmc.blink.global.annotation.Adapter;
import lombok.RequiredArgsConstructor;

@Adapter
@RequiredArgsConstructor
public class LinkFolderCommandAdapter {

    private final LinkFolderRepository linkFolderRepository;

    public LinkFolder create(LinkFolder linkFolder) {
        return linkFolderRepository.save(linkFolder);
    }
}
