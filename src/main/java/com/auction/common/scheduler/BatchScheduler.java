package com.auction.common.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.auction.common.constants.BatchConst.CHECK_EXPIRE_COUPON_JOB;

@Component
@RequiredArgsConstructor
public class BatchScheduler {

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;

    @Scheduled(cron = "0 0 0/1 * * *")    // 1시간마다 실행
    public void runRefundRetryJob() {
        String time = LocalDateTime.now().toString();
        try {
            Job job = jobRegistry.getJob("refundRetryJob");
            JobParametersBuilder jobParam = new JobParametersBuilder().addString("time", time);
            jobLauncher.run(job, jobParam.toJobParameters());
        } catch (NoSuchJobException | JobInstanceAlreadyCompleteException | JobExecutionAlreadyRunningException |
                 JobParametersInvalidException | JobRestartException e) {
            throw new RuntimeException(e);
        }
    }

    @Scheduled(cron = "0 0 0 * * ?") // 0시 0분 0초에 실행
    public void runCheckExpireCouponJob() {
        LocalDate date = LocalDate.now();
        try {
            Job job = jobRegistry.getJob(CHECK_EXPIRE_COUPON_JOB);
            JobParametersBuilder jobParam = new JobParametersBuilder()
                    .addLocalDateTime("datetime", LocalDateTime.now())
                    .addLocalDate("expireAt", date.minusDays(1));
            jobLauncher.run(job, jobParam.toJobParameters());
        } catch (NoSuchJobException | JobInstanceAlreadyCompleteException | JobExecutionAlreadyRunningException |
                 JobParametersInvalidException | JobRestartException e) {
            throw new RuntimeException(e);
        }
    }
}
