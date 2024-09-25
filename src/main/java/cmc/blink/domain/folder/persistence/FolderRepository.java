package cmc.blink.domain.folder.persistence;

import cmc.blink.domain.user.persistence.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FolderRepository extends JpaRepository<Folder, Long> {

    List<Folder> findAllByUserOrderBySortOrderAsc(User user);
    boolean existsByUserAndTitle(User user, String title);
    int countByUser(User user);

    @Query("SELECT f FROM Folder f WHERE f.user = :user ORDER BY f.lastLinkedAt DESC")
    List<Folder> findTop1ByUserOrderByLastLinkedAtDesc(@Param("user") User user);

    List<Folder> findAllByUser(User user);
}
