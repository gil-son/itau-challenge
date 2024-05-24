package com.transferencia.configs.aws;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.Topic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AwsSnsConfig {

    @Value("${aws.region}")
    private String region;
    @Value("${aws.accessKeyId}")
    private String accessKeyId;
    @Value("${aws.secretKey}")
    private String secretKey;
    @Value("${aws.sns.transferencia.topic.arn}")
    private String transferenciaFalhaTopicArn;

    @Value("${aws.sns.basen.topic.arn}")
    private String basenFalhaTopicArn;

    @Bean
    public AmazonSNS amazonSNSBuilder(){
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKeyId, secretKey);

        return AmazonSNSClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region)
                .build();
    }

    @Bean(name = "transferenciaFalhaTopic")
    public Topic snsTransferenciaFalhaTopicBuilder() {
        return new Topic().withTopicArn(transferenciaFalhaTopicArn);
    }

    @Bean(name = "basenFalhaTopic")
    public Topic snsBasenFalhaTopicBuilder() {
        return new Topic().withTopicArn(basenFalhaTopicArn);
    }

}
