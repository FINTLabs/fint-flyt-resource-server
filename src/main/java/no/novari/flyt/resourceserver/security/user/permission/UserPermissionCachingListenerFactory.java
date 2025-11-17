package no.novari.flyt.resourceserver.security.user.permission;

import lombok.extern.slf4j.Slf4j;
import no.novari.cache.FintCache;
import no.novari.kafka.consuming.ErrorHandlerConfiguration;
import no.novari.kafka.consuming.ErrorHandlerFactory;
import no.novari.kafka.consuming.ListenerConfiguration;
import no.novari.kafka.consuming.ParameterizedListenerContainerFactoryService;
import no.novari.kafka.topic.name.EntityTopicNameParameters;
import no.novari.kafka.topic.name.TopicNamePrefixParameters;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import java.util.UUID;

@Slf4j
public class UserPermissionCachingListenerFactory {

    public ConcurrentMessageListenerContainer<String, UserPermission> create(
            ParameterizedListenerContainerFactoryService containerFactoryService,
            FintCache<UUID, UserPermission> userPermissionCache,
            ErrorHandlerFactory errorHandlerFactory
    ) {
        return containerFactoryService.createBatchListenerContainerFactory(
                UserPermission.class,
                consumerRecords -> consumerRecords
                        .forEach(consumerRecord -> {
                                    log.debug(
                                            "Consuming user permission: {} {}",
                                            consumerRecord.key(),
                                            consumerRecord.value().getSourceApplicationIds()
                                    );
                                    userPermissionCache.put(
                                            UUID.fromString(consumerRecord.key()),
                                            consumerRecord.value()
                                    );
                                }
                        ),
                ListenerConfiguration.stepBuilder()
                        .groupIdApplicationDefault()
                        .maxPollRecordsKafkaDefault()
                        .maxPollIntervalKafkaDefault()
                        .seekToBeginningOnAssignment()
                        .build(),
                errorHandlerFactory.createErrorHandler(
                        ErrorHandlerConfiguration
                                .<UserPermission>stepBuilder()
                                .noRetries()
                                .skipFailedRecords()
                                .build()

                )
        ).createContainer(
                EntityTopicNameParameters.builder()
                        .topicNamePrefixParameters(TopicNamePrefixParameters
                                .stepBuilder()
                                .orgIdApplicationDefault()
                                .domainContextApplicationDefault()
                                .build()
                        )
                        .resourceName("userpermission").build()
        );
    }

}
