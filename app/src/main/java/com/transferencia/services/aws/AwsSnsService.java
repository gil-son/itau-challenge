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

import java.util.UUID;

@Service
public class AwsSnsService {

    private static final Logger logger = LoggerFactory.getLogger(AwsSnsService.class);
    private final AmazonSNS snsClient;
    private final String topicArn;

    @Autowired
    public AwsSnsService(AmazonSNS snsClient, @Qualifier("transacaoFalhaTopic") Topic transacaoFalhaTopic) {
        this.snsClient = snsClient;
        this.topicArn = transacaoFalhaTopic.getTopicArn();
    }

    public void publish(MessageDTO message) {
        if (message == null || message.message() == null || message.message().isEmpty()) {
            throw new InvalidParameterException("Não existe mensagem para enviar");
        }

        try {
            logger.info("Publicando mensagem no SNS: {}", message.message());
            PublishRequest publishRequest = new PublishRequest()
                    .withTopicArn(topicArn)
                    .withMessage(message.message())
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
