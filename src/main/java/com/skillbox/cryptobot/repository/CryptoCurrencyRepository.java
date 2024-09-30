package com.skillbox.cryptobot.repository;

import com.skillbox.cryptobot.client.entity.Subscribers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public interface CryptoCurrencyRepository extends JpaRepository<Subscribers, Long> {

    boolean existsByTelegramUserId(Long telegramUserId);

    Optional<Subscribers> findFirstByTelegramUserId(Long id);
}
