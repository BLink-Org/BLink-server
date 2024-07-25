package cmc.blink.domain.bookmark.persistence;

import cmc.blink.domain.user.persistence.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    List<Bookmark> findAllByUserOrderByCreatedAtDesc(User user);
}
