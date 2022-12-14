package no.fintlabs.resourceserver.security.client.sourceapplication;

import no.fintlabs.kafka.common.topic.TopicCleanupPolicyParameters;
import no.fintlabs.kafka.requestreply.RequestProducer;
import no.fintlabs.kafka.requestreply.RequestProducerFactory;
import no.fintlabs.kafka.requestreply.RequestProducerRecord;
import no.fintlabs.kafka.requestreply.topic.ReplyTopicNameParameters;
import no.fintlabs.kafka.requestreply.topic.ReplyTopicService;
import no.fintlabs.kafka.requestreply.topic.RequestTopicNameParameters;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SourceApplicationAuthorizationRequestService {

    private final RequestTopicNameParameters requestTopicNameParameters;
    private final RequestProducer<String, SourceApplicationAuthorization> requestProducer;

    public SourceApplicationAuthorizationRequestService(
            @Value("${fint.kafka.application-id}") String applicationId,
            RequestProducerFactory requestProducerFactory,
            ReplyTopicService replyTopicService
    ) {
        requestTopicNameParameters = RequestTopicNameParameters
                .builder()
                .resource("authorization")
                .parameterName("client-id")
                .build();

        ReplyTopicNameParameters replyTopicNameParameters = ReplyTopicNameParameters
                .builder()
                .applicationId(applicationId)
                .resource("authorization")
                .build();
        replyTopicService.ensureTopic(replyTopicNameParameters, 0, TopicCleanupPolicyParameters.builder().build());

        this.requestProducer = requestProducerFactory.createProducer(
                replyTopicNameParameters,
                String.class,
                SourceApplicationAuthorization.class
        );
    }

    public Optional<SourceApplicationAuthorization> getClientAuthorization(String clientId) {
        return requestProducer.requestAndReceive(
                RequestProducerRecord
                        .<String>builder()
                        .topicNameParameters(requestTopicNameParameters)
                        .value(clientId)
                        .build()
        ).map(ConsumerRecord::value);
    }

}
