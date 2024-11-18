package com.auction.domain.coupon.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CouponLogDto {
    private String step;
    private boolean success;
    private String duration;
    private Long readCount;
    private Long writeCount;
    private Long commitCount;

    public static CouponLogDto of(
            String step,
            boolean success,
            String duration,
            Long readCount,
            Long writeCount,
            Long commitCount
    ) {
        return new CouponLogDto(step, success, duration, readCount, writeCount, commitCount);
    }
}
