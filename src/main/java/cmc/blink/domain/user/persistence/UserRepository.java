package cmc.blink.domain.user.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.deleteRequestDate <= :sevenDaysAgo")
    List<User> findUsersDeleteRequestBefore(@Param("sevenDaysAgo") LocalDate sevenDaysAgo);
}
