package com.transferencia.services.aws;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.InvalidParameterException;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.util.UUID;


@Service
public class AwsSnsService {

    private static final Logger logger = LoggerFactory.getLogger(AwsSnsService.class);

    private final AmazonSNS snsClient;
    private final String topicArn;
    private final String transferenciaFalhaTopicArn;
    private final String basenFalhaTopicArn;

    @Autowired
    public AwsSnsService(AmazonSNS snsClient, @Qualifier("transferenciaFalhaTopic") Topic transferenciaFalhaTopic,
                         @Qualifier("basenFalhaTopic") Topic basenFalhaTopic,
                         @Value("${aws.sns.transferencia.topic.arn}") String transferenciaFalhaTopicArn,
                         @Value("${aws.sns.basen.topic.arn}") String basenFalhaTopicArn) {
        this.snsClient = snsClient;
        this.topicArn = transferenciaFalhaTopic.getTopicArn();
        this.transferenciaFalhaTopicArn = transferenciaFalhaTopicArn;
        this.basenFalhaTopicArn = basenFalhaTopicArn;
    }

    public void publicaTransferenciaFalhaTopic(String message) {
        publish(message, transferenciaFalhaTopicArn);
    }

    public void publishToBasenFalhaTopic(String message) {
        publish(message, basenFalhaTopicArn);
    }

    private void publish(String message, String topicArn) {
        if (message == null || message.isEmpty()) {
            throw new InvalidParameterException("Não existe mensagem para enviar");
        }

        try {
            logger.info("Publicando mensagem no SNS: {}", message);
            PublishRequest publishRequest = new PublishRequest()
                    .withTopicArn(topicArn)
                    .withMessage(message)
                    .withMessageGroupId("default")
                    .withMessageDeduplicationId(UUID.randomUUID().toString());

            snsClient.publish(publishRequest);
        } catch (InvalidParameterException e) {
            logger.error("Parâmetro inválido ao publicar no SNS: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Erro ao publicar no SNS: {}", e.getMessage());
            throw new RuntimeException("Erro ao publicar no SNS", e);
        }
    }
}