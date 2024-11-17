package com.auction.domain.coupon.listener;

import com.auction.domain.coupon.dto.CouponLogDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class CheckExpireCouponListener implements JobExecutionListener, StepExecutionListener {
    private final Logger logger = LoggerFactory.getLogger(CheckExpireCouponListener.class);
    private final ThreadLocal<Long> stepStartTime = new ThreadLocal<>();

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
            return ExitStatus.FAILED;
        } else {
            return ExitStatus.COMPLETED;
        }
    }

    private String requestLogDtoToString(Object logDto) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map map = objectMapper.convertValue(logDto, Map.class);
        return map.toString();
    }
}

