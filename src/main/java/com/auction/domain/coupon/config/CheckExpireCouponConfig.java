package com.auction.domain.coupon.config;

import com.auction.domain.coupon.dto.CouponDto;
import com.auction.domain.coupon.listener.AfterChunkSleepListener;
import com.auction.domain.coupon.listener.CheckExpireCouponListener;
import com.auction.domain.coupon.partitioner.CouponPartitioner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLRecoverableException;
import java.util.HashMap;
import java.util.Map;

import static com.auction.common.constants.BatchConst.*;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CheckExpireCouponConfig {
    @Value("${spring.batch.job.chunk-size}")
    private int chunkSize;

    @Value("${spring.batch.job.pool-size}")
    private int poolSize;

    @Bean(CHECK_EXPIRE_COUPON_PREFIX + JOB_PREFIX)
    public Job checkExpireCouponJob(
            JobRepository jobRepository,
            Step checkExpireCouponMasterStep,
            PlatformTransactionManager platformTransactionManager
    ) {
        return new JobBuilder(CHECK_EXPIRE_COUPON_PREFIX, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(checkExpireCouponMasterStep)
                .build();
    }

    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        int numOfCores = Runtime.getRuntime().availableProcessors();
        float targetCpuUtil = 0.3f;
        float blockingCoefficient = 0.1f;
        int threadPoolSize = Math.max(1, Math.round(numOfCores * targetCpuUtil * (1 + blockingCoefficient)));

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(threadPoolSize);
        executor.setMaxPoolSize(threadPoolSize);
        executor.setThreadNamePrefix(CHECK_EXPIRE_COUPON_PREFIX + THREAD_PREFIX);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();

        return executor;
    }

    @Bean
    public TaskExecutorPartitionHandler taskExecutorPartitionHandler(
            Step checkExpireCouponSlaveStep
    ) {
        TaskExecutorPartitionHandler partitionHandler = new TaskExecutorPartitionHandler();
        partitionHandler.setStep(checkExpireCouponSlaveStep);
        partitionHandler.setTaskExecutor(threadPoolTaskExecutor());
        partitionHandler.setGridSize(poolSize);
        return partitionHandler;
    }

    @Bean
    @StepScope
    public CouponPartitioner partitioner(
            @Qualifier(SLAVE_DATASOURCE) DataSource dataSource
    ) {
        return new CouponPartitioner(dataSource, "coupon_user", "id");
    }

    @Bean
    public Step checkExpireCouponMasterStep(
            JobRepository jobRepository,
            Step checkExpireCouponSlaveStep,
            @Qualifier(SLAVE_DATASOURCE) DataSource dataSource,
            TaskExecutorPartitionHandler taskExecutorPartitionHandler
    ) {
        return new StepBuilder(CHECK_EXPIRE_COUPON_MASTER_STEP, jobRepository)
                .partitioner(checkExpireCouponSlaveStep.getName(), partitioner(dataSource))
                .step(checkExpireCouponSlaveStep)
                .partitionHandler(taskExecutorPartitionHandler)
                .build();
    }

    @Bean
    public Step checkExpireCouponSlaveStep(
            JobRepository jobRepository,
            JdbcPagingItemReader<CouponDto> getExpireCouponReader,
            JdbcBatchItemWriter<CouponDto> deleteExpireCouponWriter,
            CheckExpireCouponListener checkExpireCouponListener,
            PlatformTransactionManager platformTransactionManager
    ) {
        return new StepBuilder(CHECK_EXPIRE_COUPON_SLAVE_STEP, jobRepository)
                .<CouponDto, CouponDto>chunk(chunkSize, platformTransactionManager)
                .reader(getExpireCouponReader)
                .writer(deleteExpireCouponWriter)
                .listener(new AfterChunkSleepListener(100))
                .listener(checkExpireCouponListener)
                .faultTolerant()
                .retryPolicy(simpleRetryPolicy())
                .backOffPolicy(fixedBackOffPolicy())
                .build();
    }

    @Bean
    public FixedBackOffPolicy fixedBackOffPolicy() {
        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(10000);
        return fixedBackOffPolicy;
    }

    @Bean
    public SimpleRetryPolicy simpleRetryPolicy() {
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        // 복구 가능한 데이터베이스 오류 발생 시 재시도
        retryableExceptions.put(SQLRecoverableException.class, true);
        // 커넥션풀 고갈, 트랜잭션 충돌, 데이터 접근 일시적 문제 발생 시 재시도
        retryableExceptions.put(TransientDataAccessException.class, true);

        return new SimpleRetryPolicy(2, retryableExceptions);
    }
}
