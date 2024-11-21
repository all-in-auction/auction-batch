package com.auction.domain.coupon.partitioner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class CouponPartitioner implements Partitioner {
    private final JdbcOperations jdbcTemplate;
    private final String table;
    private final String column;

    public CouponPartitioner(DataSource dataSource, String table, String column) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.table = table;
        this.column = column;
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        long min = getMinValue();
        long max = getMaxValue();

        if (min == 0 && max == 0) {
            return Collections.emptyMap();
        }

        Map<String, ExecutionContext> result = getExecutionContextMap(min, max, gridSize);

        return result;
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

            value.putLong("minValue", start);
            value.putLong("maxValue", end);

            start += targetSize;
            end += targetSize;
            number++;
        }
        return result;
    }

    private long getMinValue() {
        Long min = jdbcTemplate.queryForObject(
                "SELECT MIN(" + column + ") FROM " + table,
                Long.class
        );

        return min == null ? 0L : min;
    }

    private long getMaxValue() {
        Long max = jdbcTemplate.queryForObject(
                "SELECT MAX(" + column + ") FROM " + table,
                Long.class
        );

        return max == null ? 0L : max;
    }
}