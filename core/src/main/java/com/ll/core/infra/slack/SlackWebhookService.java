package com.ll.core.infra.slack;

import com.slack.api.Slack;
import com.slack.api.webhook.Payload;
import com.slack.api.webhook.WebhookResponse;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// TODO : Slack 에 대한 내용은 차후 Notify 모듈로 분리해야 함
@Service
public class SlackWebhookService {

    @Value("${slack.webhook-url:Unknown}")
    private String slackWebhookUrl;

    private final Slack slack = Slack.getInstance();

    public void sendMessage(ConsumerRecord<?, ?> record, Exception ex) {
        try {
            WebhookResponse response = slack.send(slackWebhookUrl, Payload.builder().text(getMessage(record, ex)).build());

            if (response.getCode() != 200 || !"ok".equals(response.getBody())) {
                throw new IllegalStateException(
                        "Slack webhook failed. code=" + response.getCode()
                                + ", body=" + response.getBody()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to send Slack message", e);
        }
    }

    private String getMessage(ConsumerRecord<?, ?> record, Exception ex) {
        Throwable throwable = unwrapKafkaException(ex);
        return  """
                Kafka DLQ 발생 알림
                ────────────────────────
                Topic      : %s
                Partition  : %d
                Offset     : %d
                
                Record
                %s
                
                Exception
                %s
                
                Message
                %s
                """.formatted(
                                record.topic(),
                                record.partition(),
                                record.offset(),
                                record.value(),
                                throwable.getClass().getSimpleName(),
                                throwable.getMessage()
                        );
    }

    public static Throwable unwrapKafkaException(Throwable ex) {
        Throwable cause = ex;
        while (cause instanceof org.springframework.kafka.listener.ListenerExecutionFailedException
                || cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }
}
