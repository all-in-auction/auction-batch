package com.auction.domain.slack.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class SlackMessageRequestDto {
    private String batchName;           // 배치 작업 이름
    private LocalDateTime failureTime;  // 실패 시각
    private String stepName;            // 실패한 step 이름
    private String errorCode;           // 에러 코드
    private String errorMessage;        // 에러 메시지
    private String contactPerson;       // 담당자 정보

    public static SlackMessageRequestDto of(String batchName, LocalDateTime failureTime, String stepName, String errorCode, String errorMessage, String contactPerson) {
        return new SlackMessageRequestDto(
                batchName,
                failureTime,
                stepName,
                errorCode,
                errorMessage,
                contactPerson
        );
    }
}