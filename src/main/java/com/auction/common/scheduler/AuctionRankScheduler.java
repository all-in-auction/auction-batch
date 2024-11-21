package com.auction.common.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class AuctionRankScheduler {
    private final RedisTemplate<String, Object> redisTemplate;
    public static final String AUCTION_RANKING_PREFIX = "auction:ranking:";

    // 아침 6시에 경매 랭킹 초기화
    @Scheduled(cron = "0 0 6 * * *")
    @Transactional
    public void resetRankings() {
        redisTemplate.delete(AUCTION_RANKING_PREFIX);
    }
}
