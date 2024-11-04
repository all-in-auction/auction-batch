package com.auction.domain.auction.repository;

import com.auction.domain.auction.entity.RefundLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundLogRepository extends JpaRepository<RefundLog, Long> {
}
