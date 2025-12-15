package com.ll.core.infra.slack;

import com.slack.api.Slack;
import com.slack.api.webhook.Payload;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// TODO : Slack 에 대한 내용은 차후 Notify 모듈로 분리해야 함
@Service
@Slf4j
public class SlackWebhookService {

    @Value("${slack.webhook-url:Unknown}")
    private String slackWebhookUrl;

    private final Slack slack = Slack.getInstance();

    public void sendMessage(ConsumerRecord<?, ?> record, Exception ex) {
        if ( "Unknown".equals(slackWebhookUrl) ) {
            return;
        }
        String message = getMessage(record, ex);

        try {
            System.out.println(message);
            slack.send(slackWebhookUrl, Payload.builder().text(message).build());
        } catch (Exception e) {
            log.error("Failed to send Slack webhook message : {}", message, e);
        }
    }

    private String getMessage(ConsumerRecord<?, ?> record, Exception ex) {
        String exception = getUnwrapException(ex);
        return  """
                ──────────────────────────
                Kafka DLQ 발생 알림
                ──────────────────────────
                Topic      : %s
                Partition  : %d
                Offset     : %d
                
                Record:
                %s
                
                Exception:
                %s
                """.formatted(
                                record.topic(),
                                record.partition(),
                                record.offset(),
                                record.value(),
                                exception
                        );
    }

    private String getUnwrapException(Throwable ex) {
        StringBuilder sb = new StringBuilder();

        Throwable current = ex;
        int depth = 0;

        while (current != null && depth < 5) {
            sb.append("[")
                    .append(current.getClass().getSimpleName())
                    .append("]\n-> ")
                    .append(current.getMessage())
                    .append("\n\n");

            current = current.getCause();
            depth++;
        }

        return sb.toString();
    }

}
