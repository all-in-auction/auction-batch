package com.auction.domain.coupon.listener;

import com.auction.domain.coupon.dto.CouponLogDto;
import com.auction.domain.slack.SlackUtils;
import com.auction.domain.slack.dto.request.SlackMessageRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CheckExpireCouponListener implements JobExecutionListener, StepExecutionListener {
    private final Logger logger = LoggerFactory.getLogger(CheckExpireCouponListener.class);
    private final ThreadLocal<Long> stepStartTime = new ThreadLocal<>();

    private final ObjectMapper objectMapper;
    private final SlackUtils slackUtils;

    @Override
    public void beforeStep(@NonNull StepExecution stepExecution) {
        stepStartTime.set(System.currentTimeMillis());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        long duration = System.currentTimeMillis() - stepStartTime.get();
        stepStartTime.remove();

        CouponLogDto couponLogDto = CouponLogDto.of(
                stepExecution.getStepName(),
                stepExecution.getStatus() == BatchStatus.COMPLETED,
                duration + "ms",
                stepExecution.getReadCount(),
                stepExecution.getWriteCount(),
                stepExecution.getCommitCount()
        );

        logger.info(requestLogDtoToString(couponLogDto));

        if (stepExecution.getStatus() == BatchStatus.FAILED) {
            SlackMessageRequestDto slackMessageRequestDto = SlackMessageRequestDto.of(
                    stepExecution.getJobExecution().getJobInstance().getJobName(),
                    stepExecution.getLastUpdated(),
                    stepExecution.getStepName(),
                    stepExecution.getExitStatus().getExitCode(),
                    stepExecution.getFailureExceptions().get(0).getCause().toString(),
                    "김나람"
            );

            slackUtils.sendMessage(slackMessageRequestDto);

            return ExitStatus.FAILED;
        } else {
            return ExitStatus.COMPLETED;
        }
    }

    private String requestLogDtoToString(Object logDto) {
        return objectMapper.convertValue(logDto, Map.class).toString();
    }
}

