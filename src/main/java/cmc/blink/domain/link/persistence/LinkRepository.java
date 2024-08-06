package cmc.blink.domain.link.persistence;

import cmc.blink.domain.user.persistence.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LinkRepository extends JpaRepository<Link, Long> {
    Optional<Link> findById(Long id);
    boolean existsByUrlAndUser(String url, User user);
    int countByUser(User user);

    @Query("SELECT COUNT(l) FROM Link l WHERE l.user = :user AND l.id NOT IN (SELECT lf.link.id FROM LinkFolder lf)")
    int countByUserAndNoFolder(@Param("user") User user);

    Page<Link> findByIdInAndUserAndIsTrashFalse(List<Long> ids, User user, Pageable pageable);

    Page<Link> findByUserAndIsTrashFalse(User user, Pageable pageable);
    int countByUserAndIsTrashFalse(User user);

    Page<Link> findByUserAndIsPinnedTrueAndIsTrashFalse(User user, Pageable pageable);
    int countByUserAndIsPinnedTrueAndIsTrashFalse(User user);

    Page<Link> findByUserAndIsTrashTrue(User user, Pageable pageable);
    int countByUserAndIsTrashTrue(User user);
}
