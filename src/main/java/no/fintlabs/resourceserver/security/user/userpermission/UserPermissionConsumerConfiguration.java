package no.fintlabs.resourceserver.security.user.userpermission;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.cache.FintCache;
import no.fintlabs.kafka.consuming.ErrorHandlerConfiguration;
import no.fintlabs.kafka.consuming.ErrorHandlerFactory;
import no.fintlabs.kafka.consuming.ListenerConfiguration;
import no.fintlabs.kafka.consuming.ParameterizedListenerContainerFactoryService;
import no.fintlabs.kafka.topic.name.EntityTopicNameParameters;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import java.util.UUID;


@Configuration
@Import(
        {
                ParameterizedListenerContainerFactoryService.class,
                ErrorHandlerFactory.class,
                UserPermissionCacheConfiguration.class
        }
)
@Slf4j
public class UserPermissionConsumerConfiguration {
    private final ParameterizedListenerContainerFactoryService containerFactoryService;
    private final FintCache<UUID, UserPermission> userPermissionCache;
    private final ErrorHandlerFactory errorHandlerFactory;

    public UserPermissionConsumerConfiguration(
            ParameterizedListenerContainerFactoryService containerFactoryService,
            FintCache<UUID, UserPermission> userPermissionCache,
            ErrorHandlerFactory errorHandlerFactory
    ) {
        this.containerFactoryService = containerFactoryService;
        this.userPermissionCache = userPermissionCache;
        this.errorHandlerFactory = errorHandlerFactory;
    }

    @Bean
    @ConditionalOnProperty(value = "fint.flyt.resource-server.user-permissions-consumer.enabled", havingValue = "true")
    ConcurrentMessageListenerContainer<String, UserPermission> createCacheConsumer() {
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
        ).createContainer(EntityTopicNameParameters.builder().resourceName("userpermission").build());
    }

}