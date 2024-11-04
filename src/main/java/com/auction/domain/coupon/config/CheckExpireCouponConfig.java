package com.auction.domain.coupon.config;

import com.auction.domain.coupon.dto.CouponDto;
import com.auction.domain.coupon.listener.CheckExpireCouponListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import static com.auction.common.constants.BatchConst.CHECK_EXPIRE_COUPON_JOB;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CheckExpireCouponConfig {

    @Value("${spring.batch.job.chunk-size}")
    private int chunkSize;

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
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        int numOfCores = Runtime.getRuntime().availableProcessors();
        float targetCpuUtil = 0.3f;
        float blockingCoefficient = 0.1f;
        int threadPoolSize = Math.round(numOfCores * targetCpuUtil * (1 + blockingCoefficient));

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(threadPoolSize);
        executor.setMaxPoolSize(threadPoolSize);
        executor.setThreadNamePrefix("expire-coupon-thread-");
        executor.initialize();

        return executor;
    }

    @Bean
    public Step checkExpireCouponStep(
            JobRepository jobRepository,
            JdbcPagingItemReader<CouponDto> getExpireCouponReader,
            JdbcBatchItemWriter<CouponDto> deleteExpireCouponWriter,
            CheckExpireCouponListener checkExpireCouponListener,
            PlatformTransactionManager platformTransactionManager
    ) {
        return new StepBuilder("checkExpireCouponStep", jobRepository)
                .<CouponDto, CouponDto>chunk(chunkSize, platformTransactionManager)
                .reader(getExpireCouponReader)
                .writer(deleteExpireCouponWriter)
                .listener(checkExpireCouponListener)
                .taskExecutor(threadPoolTaskExecutor())
                .build();
    }
}
