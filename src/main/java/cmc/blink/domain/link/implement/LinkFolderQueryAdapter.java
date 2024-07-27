package cmc.blink.domain.link.implement;

import cmc.blink.domain.link.persistence.LinkFolderRepository;
import cmc.blink.global.annotation.Adapter;
import lombok.RequiredArgsConstructor;

@Adapter
@RequiredArgsConstructor
public class LinkFolderQueryAdapter {

    private final LinkFolderRepository linkFolderRepository;

}
