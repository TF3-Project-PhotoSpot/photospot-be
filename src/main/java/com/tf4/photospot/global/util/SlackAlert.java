package com.tf4.photospot.global.util;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.slack.api.Slack;
import com.slack.api.model.Attachment;
import com.slack.api.model.Field;
import com.slack.api.webhook.Payload;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SlackAlert {
	private final Slack slackClient = Slack.getInstance();

	@Value("${slack.url}")
	private String webhookUrl;

	@Async
	public void sendKakaoCallbackFailure(Exception reason, String account, String refererType) {
		try {
			slackClient.send(webhookUrl,
				Payload.builder()
					.text("ℹ %s" .formatted("카카오 연결 끊기 콜백 처리 실패"))
					.attachments(
						List.of(generateKakaoFailureLog(reason, account, refererType)))
					.build());
		} catch (IOException ex) {
			log.debug("info log slack 전송 중 예외 발생", ex);
		}
	}

	private Attachment generateKakaoFailureLog(Exception ex, String account, String refererType) {
		String requestTime = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss").format(LocalDateTime.now());
		return Attachment.builder()
			.color("007Bff")
			.title(requestTime + "에러 발생")
			.fields(List.of(
					generateField("User Account", account),
					generateField("Referer Type", refererType),
					generateField("Error Type", ex.getClass().getName()),
					generateField("Error Message", ex.getMessage())
				)
			).build();
	}

	private Field generateField(String title, String value) {
		return Field.builder()
			.title(title)
			.value(value)
			.valueShortEnough(false)
			.build();
	}
}
