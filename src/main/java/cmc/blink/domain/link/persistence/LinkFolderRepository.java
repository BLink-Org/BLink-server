package cmc.blink.domain.link.persistence;

import cmc.blink.domain.folder.persistence.Folder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LinkFolderRepository extends JpaRepository<LinkFolder, Long> {

    List<LinkFolder> findAllByFolder(Folder folder);
    List<LinkFolder> findAllByLink(Link link);

    int countByFolder(Folder folder);

    Page<LinkFolder> findByFolderAndLinkIsTrashFalse(Folder folder, Pageable pageable);

    int countByFolderAndLinkIsTrashFalse(Folder folder);

    @Query("SELECT lf.link.id, f.title FROM LinkFolder lf JOIN lf.folder f WHERE lf.link.id IN :linkIds ORDER BY lf.folder.sortOrder")
    List<Object[]> findFolderTitlesForLinks(@Param("linkIds") List<Long> linkIds);
}
