package com.auction.domain.coupon.listener;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CheckExpireCouponListener implements JobExecutionListener, StepExecutionListener {
    private static final Logger log = LoggerFactory.getLogger(CheckExpireCouponListener.class);
    private final ThreadLocal<Long> stepStartTime = new ThreadLocal<>();

    @Override
    public void beforeStep(@NonNull StepExecution stepExecution) {
        stepStartTime.set(System.currentTimeMillis());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        long duration = System.currentTimeMillis() - stepStartTime.get();
        stepStartTime.remove();

        log.info("Step {} . Duration: {} ms, Read count: {}, Write count: {}, Commit count: {}",
                stepExecution.getStepName(), duration, stepExecution.getReadCount(),
                stepExecution.getWriteCount(), stepExecution.getCommitCount());

        if (stepExecution.getStatus() == BatchStatus.FAILED) {
            return ExitStatus.FAILED;
        } else {
            return ExitStatus.COMPLETED;
        }
    }
}

