package com.auction.domain.slack.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SlackMessageType {
    INFO("알림"),
    FAIL("실패");

    private final String type;
}
