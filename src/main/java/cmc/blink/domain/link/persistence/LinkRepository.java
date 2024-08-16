package cmc.blink.domain.link.persistence;

import cmc.blink.domain.user.persistence.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LinkRepository extends JpaRepository<Link, Long> {
    Optional<Link> findById(Long id);

    Optional<Link> findByUserAndUrl(User user, String url);

    boolean existsByUrlAndUser(String url, User user);
    int countByUser(User user);

    @Query("SELECT COUNT(l) FROM Link l WHERE l.user = :user AND l.isTrash = false AND l.id NOT IN (SELECT lf.link.id FROM LinkFolder lf)")
    int countByUserAndNoFolderAndIsTrashFalse(@Param("user") User user);

    @Query("SELECT l FROM Link l WHERE l.user = :user AND l.isTrash = false AND l.id NOT IN (SELECT lf.link.id FROM LinkFolder lf)")
    Page<Link> findByUserAndNoFolderAndIsTrashFalse(@Param("user") User user, Pageable pageable);

    Page<Link> findByIdInAndUserAndIsTrashFalse(List<Long> ids, User user, Pageable pageable);
    Page<Link> findByUserAndIsTrashFalse(User user, Pageable pageable);

    int countByUserAndIsTrashFalse(User user);
    Page<Link> findByUserAndIsPinnedTrueAndIsTrashFalse(User user, Pageable pageable);

    int countByUserAndIsPinnedTrueAndIsTrashFalse(User user);
    Page<Link> findByUserAndIsTrashTrue(User user, Pageable pageable);

    int countByUserAndIsTrashTrue(User user);
    @Query(value = "SELECT l FROM Link l WHERE l.user = :user AND l.isTrash = false AND l.isExcluded = false " +
            "AND l.lastViewedAt IS NOT NULL AND l.lastViewedAt >= :recentDate " +
            "ORDER BY l.lastViewedAt DESC")
    List<Link> findTop5LastViewedLinksByUser(@Param("user") User user, @Param("recentDate") LocalDateTime recentDate, Pageable pageable);

    @Query("SELECT l FROM Link l WHERE l.user = :user AND l.isTrash = false " +
            "AND (l.title LIKE %:query% OR l.contents LIKE %:query% OR l.url LIKE %:query% OR l.type LIKE %:query%)")
    Page<Link> searchLinksByUserAndQuery(@Param("user") User user, @Param("query") String query, Pageable pageable);

}
