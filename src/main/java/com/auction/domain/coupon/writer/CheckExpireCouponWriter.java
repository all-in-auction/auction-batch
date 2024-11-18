package com.auction.domain.coupon.writer;

import com.auction.domain.coupon.dto.CouponDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

import static com.auction.common.constants.BatchConst.MASTER_DATASOURCE;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CheckExpireCouponWriter {

    @Bean
    @StepScope
    public JdbcBatchItemWriter<CouponDto> deleteExpireCouponWriter(
            @Qualifier(MASTER_DATASOURCE) DataSource dataSource
    ) {
        return new JdbcBatchItemWriterBuilder<CouponDto>()
                .dataSource(dataSource)
                .sql("UPDATE coupon_user SET is_available = false WHERE id = :id AND is_available = true")
                .beanMapped()
                // 하나의 행이라도 업데이트/삭제가 없다면 예외 throw 하지 않도록 설정
                .assertUpdates(false)
                .build();
    }
}