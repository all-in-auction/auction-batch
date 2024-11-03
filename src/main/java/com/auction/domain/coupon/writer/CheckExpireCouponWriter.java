package com.auction.domain.coupon.writer;

import com.auction.domain.coupon.entity.CouponUser;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CheckExpireCouponWriter {
    @Bean
    @StepScope
    public JpaItemWriter<CouponUser> deleteExpireCouponWriter(
            EntityManagerFactory entityManagerFactory
    ) {
        return new JpaItemWriterBuilder<CouponUser>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }
}