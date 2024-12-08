package com.auction.domain.slack;

import com.auction.domain.slack.dto.request.SlackMessageRequestDto;
import com.auction.domain.slack.enums.SlackMessageType;
import com.slack.api.Slack;
import com.slack.api.model.block.DividerBlock;
import com.slack.api.model.block.HeaderBlock;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.composition.MarkdownTextObject;
import com.slack.api.model.block.composition.PlainTextObject;
import com.slack.api.webhook.Payload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

import static com.slack.api.webhook.WebhookPayloads.payload;

@Slf4j
@Component
@RequiredArgsConstructor
public class SlackUtils {
    @Value("${notification.slack.token}")
    private String SLACK_URL;

    @Value("${spring.application.name}")
    private String APP_NAME;

    private final Slack slackClient = Slack.getInstance();

    /**
     * send Slack message
     *
     * @param slackMessageRequestDto
     **/
    public void sendMessage(SlackMessageRequestDto slackMessageRequestDto) {
        try {
            slackClient.send(SLACK_URL, createPayload(slackMessageRequestDto));
        } catch (IOException e) {
            log.error("Slack Send Message Failed", e);
        }
    }

    /**
     * create Slack payload
     *
     * @param slackMessageRequestDto
     **/
    private Payload createPayload(SlackMessageRequestDto slackMessageRequestDto) {
        return payload(p -> p
                .blocks(List.of(
                                HeaderBlock.builder()
                                        .text(PlainTextObject.builder()
                                                .text("[" + APP_NAME + "][" + SlackMessageType.FAIL.getType() + " :warning:]")
                                                .emoji(true)
                                                .build()
                                        ).build(),
                                SectionBlock.builder()
                                        .fields(
                                                List.of(
                                                        MarkdownTextObject.builder()
                                                                .text("*작업 이름:*\n" + slackMessageRequestDto.getBatchName())
                                                                .build(),
                                                        MarkdownTextObject.builder()
                                                                .text("*실패 시각:*\n" + slackMessageRequestDto.getFailureTime())
                                                                .build(),
                                                        MarkdownTextObject.builder()
                                                                .text("*Step 이름:*\n" + slackMessageRequestDto.getStepName())
                                                                .build(),
                                                        MarkdownTextObject.builder()
                                                                .text("*에러 메시지:*\n" + slackMessageRequestDto.getErrorMessage())
                                                                .build()
                                                )
                                        ).build(),
                                DividerBlock.builder().build(),
                                SectionBlock.builder()
                                        .text(
                                                MarkdownTextObject.builder()
                                                        .text("*조치 안내:*\n로그를 확인하고 재실행하세요. 문제가 지속되면 @" + slackMessageRequestDto.getContactPerson() + " 에게 문의하세요.")
                                                        .build()
                                        ).build()
                        )
                )
        );
    }
}
