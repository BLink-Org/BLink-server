package cmc.blink.domain.folder.persistence;

import cmc.blink.domain.user.persistence.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FolderRepository extends JpaRepository<Folder, Long> {

    boolean existsByUserAndTitle(User user, String title);
    int countByUser(User user);
}
