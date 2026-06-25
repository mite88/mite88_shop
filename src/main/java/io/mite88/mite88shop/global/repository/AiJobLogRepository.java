package io.mite88.mite88shop.global.repository;

import io.mite88.mite88shop.global.model.AiJobLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiJobLogRepository extends JpaRepository<AiJobLog, Long> {
}
