package com.auction.domain.coupon.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.scope.context.ChunkContext;

import java.util.concurrent.TimeUnit;

@Slf4j
public class AfterChunkSleepListener implements ChunkListener {
    private final long sleepMillis;

    public AfterChunkSleepListener(long sleepMillis) {
        this.sleepMillis = sleepMillis;
    }

    @Override
    public void afterChunk(ChunkContext context) {
        try {
            log.info("Chunk 실행 후 sleep {} millis. 현재 read Count : {}",
                    sleepMillis,
                    context.getStepContext().getStepExecution().getReadCount());
            TimeUnit.MILLISECONDS.sleep(sleepMillis);
        } catch (InterruptedException e) {
            log.error("Thread sleep interrupted.", e);
        }
    }

    @Override
    public void afterChunkError(ChunkContext context) {
        // 사용안함.
    }

    @Override
    public void beforeChunk(ChunkContext context) {
        // 사용안함.
    }
}
