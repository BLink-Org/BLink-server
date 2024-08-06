package cmc.blink.domain.link.implement;

import cmc.blink.domain.folder.persistence.Folder;
import cmc.blink.domain.link.persistence.Link;
import cmc.blink.domain.link.persistence.LinkFolder;
import cmc.blink.domain.link.persistence.LinkFolderRepository;
import cmc.blink.global.annotation.Adapter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Adapter
@RequiredArgsConstructor
public class LinkFolderQueryAdapter {

    private final LinkFolderRepository linkFolderRepository;

    public Page<LinkFolder> findLinksByFolderAndIsTrashFalse(Folder folder, Pageable pageable) {
        return linkFolderRepository.findByFolderAndLinkIsTrashFalse(folder, pageable);
    }

    public int countByFolderAndIsTrashFalse(Folder folder) {
        return linkFolderRepository.countByFolderAndLinkIsTrashFalse(folder);
    }

    public Map<Long, String> findFirstFolderNamesForLinks(List<Link> links) {
        List<Long> linkIds = links.stream().map(Link::getId).collect(Collectors.toList());
        return linkFolderRepository.findFirstFolderNamesForLinks(linkIds).stream()
                .collect(Collectors.toMap(
                        result -> (Long) result[0],
                        result -> (String) result[1],
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }

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
