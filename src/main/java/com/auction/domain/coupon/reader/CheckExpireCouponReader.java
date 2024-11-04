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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CheckExpireCouponReader {
    private final DataSource dataSource;

    @Value("${spring.batch.job.chunk-size}")
    private int chunkSize;

//    @Bean
//    @StepScope
//    public JpaPagingItemReader<CouponUser> getExpireCouponReader(
//            EntityManagerFactory entityManagerFactory,
//            @Value("#{jobParameters['expireAt']}") LocalDate expireAt
//    ) {
//        HashMap<String, Object> parameters = new HashMap<>();
//        parameters.put("expireAt", expireAt);
//
//        return new JpaPagingItemReaderBuilder<CouponUser>()
//                .name("couponIdPagingReader")
//                .pageSize(1000)
//                .entityManagerFactory(entityManagerFactory)
//                .queryString("SELECT u FROM Coupon c JOIN CouponUser u ON c.id = u.coupon.id " +
//                        "WHERE c.expireAt = :expireAt ORDER BY u.id")
//                .parameterValues(parameters)
//                .build();
//    }

    @Bean
    @StepScope
    public JdbcPagingItemReader<CouponDto> getExpireCouponReader(
            PagingQueryProvider queryProvider,
            @Value("#{jobParameters['expireAt']}") LocalDate expireAt
    ) {
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("expire_at", expireAt);

        return new JdbcPagingItemReaderBuilder<CouponDto>()
                .name("couponIdPagingReader")
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
    public PagingQueryProvider queryProvider() throws Exception {
        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(dataSource);
        queryProvider.setSelectClause("SELECT u.id");
        queryProvider.setFromClause("FROM coupon c JOIN coupon_user u ON c.id = u.coupon_id");
        queryProvider.setWhereClause("WHERE c.expire_at = :expire_at AND u.is_available = true");

        Map<String, Order> sortKeys = new HashMap<>(1);
        sortKeys.put("u.id", Order.ASCENDING);

        queryProvider.setSortKeys(sortKeys);
        return queryProvider.getObject();
    }
}
