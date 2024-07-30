package cmc.blink.domain.link.persistence;

import cmc.blink.domain.folder.persistence.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LinkFolderRepository extends JpaRepository<LinkFolder, Long> {

    List<LinkFolder> findAllByFolder(Folder folder);
    List<LinkFolder> findAllByLink(Link link);
}
