package com.auction.domain.deposit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DepositService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final String DEPOSIT = "deposit";
    private String key(long userId, long auctionId) {
        return "auctionId:" + auctionId + ":userId:" + userId;
    }

    public void deleteDeposit(long userId, long auctionId) {
        redisTemplate.opsForHash().delete(key(userId, auctionId), DEPOSIT);
    }
}
