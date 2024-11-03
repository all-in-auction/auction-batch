package com.auction.domain.coupon.config;

import com.auction.domain.coupon.entity.CouponUser;
import com.auction.domain.coupon.listener.CheckExpireCouponListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import static com.auction.common.constants.BatchConst.CHECK_EXPIRE_COUPON_JOB;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CheckExpireCouponConfig {
    @Bean
    public Job checkExpireCouponJob(
            JobRepository jobRepository,
            Step getExpireCouponIds,
            PlatformTransactionManager platformTransactionManager
    ) {
        return new JobBuilder(CHECK_EXPIRE_COUPON_JOB, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(getExpireCouponIds)
                .build();
    }

    @Bean
    public Step checkExpireCouponStep(
            JobRepository jobRepository,
            JpaPagingItemReader<CouponUser> getExpireCouponReader,
            JpaItemWriter<CouponUser> deleteExpireCouponWriter,
            CheckExpireCouponListener checkExpireCouponListener,
            PlatformTransactionManager platformTransactionManager
    ) {
        return new StepBuilder("checkExpireCouponStep", jobRepository)
                .<CouponUser, CouponUser>chunk(1000, platformTransactionManager)
                .reader(getExpireCouponReader)
                .processor(couponUser -> {
                    couponUser.changeUnavailable();
                    return couponUser;
                })
                .writer(deleteExpireCouponWriter)
                .listener(checkExpireCouponListener)
                .build();
    }
}
