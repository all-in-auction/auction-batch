package com.auction.domain.coupon.reader;

import com.auction.domain.coupon.dto.CouponDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static com.auction.common.constants.BatchConst.CHECK_EXPIRE_COUPON_PAGING_READER;
import static com.auction.common.constants.BatchConst.SLAVE_DATASOURCE;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CheckExpireCouponReader {
    @Value("${spring.batch.job.chunk-size}")
    private int chunkSize;

    @Bean
    @StepScope
    public JdbcPagingItemReader<CouponDto> getExpireCouponReader(
            PagingQueryProvider queryProvider,
            @Qualifier(SLAVE_DATASOURCE) DataSource dataSource,
            @Value("#{stepExecutionContext['minValue']}") Long minValue,
            @Value("#{stepExecutionContext['maxValue']}") Long maxValue,
            @Value("#{jobParameters['expireAt']}") LocalDate expireAt
    ) {
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("expire_at", expireAt);
        parameters.put("min_value", minValue);
        parameters.put("max_value", maxValue);

        return new JdbcPagingItemReaderBuilder<CouponDto>()
                .name(CHECK_EXPIRE_COUPON_PAGING_READER)
                .pageSize(chunkSize)
                .fetchSize(chunkSize)
                .dataSource(dataSource)
                .rowMapper(new BeanPropertyRowMapper<>(CouponDto.class))
                .queryProvider(queryProvider)
                .parameterValues(parameters)
                .saveState(false)
                .build();
    }

    @Bean
    public PagingQueryProvider queryProvider(
            @Qualifier(SLAVE_DATASOURCE) DataSource dataSource
    ) throws Exception {
        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(dataSource);
        queryProvider.setSelectClause("SELECT u.id");
        queryProvider.setFromClause("FROM coupon c JOIN coupon_user u ON c.id = u.coupon_id");
        queryProvider.setWhereClause("WHERE c.expire_at = :expire_at AND u.is_available = true " +
                "AND u.id >= :min_value AND u.id <= :max_value");

        Map<String, Order> sortKeys = new HashMap<>(1);
        sortKeys.put("u.id", Order.ASCENDING);

        queryProvider.setSortKeys(sortKeys);
        return queryProvider.getObject();
    }
}
