package cmc.blink.domain.user.persistence.redis;

import org.springframework.data.repository.CrudRepository;

public interface BlackListTokenRepository extends CrudRepository<BlackListToken, String> {
}
