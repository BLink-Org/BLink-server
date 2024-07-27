package cmc.blink.domain.link.business;

import cmc.blink.domain.folder.persistence.Folder;
import cmc.blink.domain.link.persistence.Link;
import cmc.blink.domain.link.persistence.LinkFolder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LinkFolderMapper {

    public static LinkFolder toLinkFolder(Link link, Folder folder) {
        return LinkFolder.builder()
                .link(link)
                .folder(folder)
                .build();
    }
}
