package com.auction.domain.point.service;

import com.auction.common.apipayload.status.ErrorStatus;
import com.auction.common.exception.ApiException;
import com.auction.domain.point.entity.Point;
import com.auction.domain.point.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointService {
    private final PointRepository pointRepository;

    private Point getPoint(long userId) {
        return pointRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(ErrorStatus._INVALID_REQUEST));
    }

    @Transactional
    public void increasePoint(long userId, int price) {
        Point point = getPoint(userId);
        int newPointAmount = point.getPointAmount() + price;
        point.changePoint(newPointAmount);
        pointRepository.save(point);
    }

}
