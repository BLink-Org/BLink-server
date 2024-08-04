package cmc.blink.domain.link.implement;

import cmc.blink.domain.folder.persistence.Folder;
import cmc.blink.domain.link.persistence.Link;
import cmc.blink.domain.link.persistence.LinkFolder;
import cmc.blink.domain.link.persistence.LinkFolderRepository;
import cmc.blink.global.annotation.Adapter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Adapter
@RequiredArgsConstructor
public class LinkFolderQueryAdapter {

    private final LinkFolderRepository linkFolderRepository;

    public List<LinkFolder> findAllByFolder(Folder folder) {
        return linkFolderRepository.findAllByFolder(folder);
    }

    public List<LinkFolder> findAllByLink(Link link) {
        return linkFolderRepository.findAllByLink(link);
    }

    public boolean isOnlyLinkFolder(Link link) {
        return findAllByLink(link).isEmpty();
    }

    public int countByFolder(Folder folder) {
        return linkFolderRepository.countByFolder(folder);
    }

}
