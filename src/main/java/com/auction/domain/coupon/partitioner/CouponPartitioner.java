package com.auction.domain.coupon.partitioner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class CouponPartitioner implements Partitioner {
    private final JdbcOperations jdbcTemplate;
    private final LocalDate expireAt;

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        // 조건에 맞는 최소, 최대 ID 조회
        long start = getStart();
        long end = getEnd();
        return getExecutionContextMap(start, end, gridSize);
    }

    private long getStart() {
        Long start = jdbcTemplate.queryForObject(
                "SELECT MIN(u.id) FROM coupon c JOIN coupon_user u ON c.id = u.coupon_id " +
                        "WHERE c.expire_at = ? AND u.is_available = true",
                Long.class,
                expireAt
        );

        return start == null ? 0 : start;
    }

    private long getEnd() {
        Long end = jdbcTemplate.queryForObject(
                "SELECT MAX(u.id) FROM coupon c JOIN coupon_user u ON c.id = u.coupon_id " +
                        "WHERE c.expire_at = ? AND u.is_available = true",
                Long.class,
                expireAt
        );

        return end == null ? 0 : end;
    }

    private static Map<String, ExecutionContext> getExecutionContextMap(long min, long max, int gridSize) {
        Map<String, ExecutionContext> result = new HashMap<>();

        long targetSize = (max - min) / gridSize + 1;
        long number = 0;
        long start = min;
        long end = start + targetSize - 1;

        while (start <= max) {
            ExecutionContext value = new ExecutionContext();
            result.put("partition" + number, value);

            if (end >= max) {
                end = max;
            }

            value.putLong("start", start);
            value.putLong("end", end);

            start += targetSize;
            end += targetSize;
            number++;
        }
        return result;
    }
}