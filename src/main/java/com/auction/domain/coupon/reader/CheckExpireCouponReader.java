package com.auction.domain.coupon.reader;

import com.auction.domain.coupon.entity.CouponUser;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.HashMap;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CheckExpireCouponReader {
    @Bean
    @StepScope
    public JpaPagingItemReader<CouponUser> getExpireCouponReader(
            EntityManagerFactory entityManagerFactory,
            @Value("#{jobParameters['expireAt']}") LocalDate expireAt
    ) {
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("expireAt", expireAt);

        return new JpaPagingItemReaderBuilder<CouponUser>()
                .name("couponIdPagingReader")
                .pageSize(1000)
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT u FROM Coupon c JOIN CouponUser u ON c.id = u.coupon.id " +
                        "WHERE c.expireAt = :expireAt ORDER BY u.id")
                .parameterValues(parameters)
                .build();
    }

}
