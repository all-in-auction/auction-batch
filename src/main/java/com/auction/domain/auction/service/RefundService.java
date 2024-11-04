package com.auction.domain.auction.service;

import com.auction.domain.deposit.service.DepositService;
import com.auction.domain.point.service.PointService;
import com.auction.domain.pointHistory.enums.PaymentType;
import com.auction.domain.pointHistory.service.PointHistoryService;
import com.auction.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefundService {
    private final DepositService depositService;
    private final PointService pointService;
    private final PointHistoryService pointHistoryService;

    @Transactional
    public void processRefund(Long userId, Long auctionId, int price) {
        depositService.deleteDeposit(userId, auctionId);
        pointService.increasePoint(userId, price);
        pointHistoryService.createPointHistory(User.fromUserId(userId), price, PaymentType.REFUND);
    }
}
