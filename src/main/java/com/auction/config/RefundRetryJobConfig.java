package com.auction.config;

import com.auction.domain.auction.entity.RefundLog;
import com.auction.domain.auction.service.RefundService;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class RefundRetryJobConfig {

    private final RefundService refundService;
    private final EntityManagerFactory emf;
    private final DataSource dataSource;

    @Bean
    public Job refundRetryJob(JobRepository jobRepository, Step refundRetryStep) {
        return new JobBuilder("refundRetryJob", jobRepository)
                .start(refundRetryStep)
                .build();
    }

    @Bean
    public Step refundRetryStep(JobRepository jobRepository, Tasklet testTasklet, PlatformTransactionManager platformTransactionManager) {
        return new StepBuilder("refundRetryStep", jobRepository)
                .<RefundLog, RefundLog>chunk(100, platformTransactionManager)
                .reader(failedRefundReader(emf))
                .processor(failedRefundProcessor())
                .writer(failedRefundWriter(dataSource))
                .build();
    }

    @Bean
    public Tasklet testTasklet(){
        return ((contribution, chunkContext) -> RepeatStatus.FINISHED);
    }

    @Bean
    public ItemProcessor<RefundLog, RefundLog> failedRefundProcessor() {
        return refundLog -> {
            try {
                if(!refundLog.isConsumed()) {
                    // 환불 재시도
                    refundService.processRefund(refundLog.getUser().getId(),
                            refundLog.getAuction().getId(),
                            refundLog.getPrice()
                    );
                    return refundLog;
                }
                return null;
            } catch (Exception e) {
                // Writer 제외
                return null;
            }
        };
    }

    // 환불 실패 데이터 읽어오기
    @Bean
    public JpaPagingItemReader<RefundLog> failedRefundReader(EntityManagerFactory emf) {
        return new JpaPagingItemReaderBuilder<RefundLog>()
                .name("failedRefundReader")
                .entityManagerFactory(emf)
                .queryString("SELECT r FROM RefundLog r WHERE r.isConsumed = false")
                .pageSize(10)
                .build();
    }

    // 성공적으로 처리된 후 모두 삭제
    @Bean
    public ItemWriter<RefundLog> failedRefundWriter(DataSource dataSource) {
        return items -> {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            jdbcTemplate.update("DELETE FROM refund_log");
        };
    }
}
