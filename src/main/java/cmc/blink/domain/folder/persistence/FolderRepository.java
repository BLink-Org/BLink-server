package cmc.blink.domain.folder.persistence;

import cmc.blink.domain.user.persistence.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FolderRepository extends JpaRepository<Folder, Long> {

    List<Folder> findAllByUserOrderBySortOrderAsc(User user);
    boolean existsByUserAndTitle(User user, String title);
    int countByUser(User user);
}
