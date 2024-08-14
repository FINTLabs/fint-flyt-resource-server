package no.fintlabs.resourceserver.security.user.userpermission;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.cache.FintCache;
import no.fintlabs.kafka.entity.EntityConsumerFactoryService;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;


@Configuration
@Import(
        {
                EntityConsumerFactoryService.class,
                UserPermissionCacheConfiguration.class
        }
)
@Slf4j
public class UserPermissionConsumerConfiguration {
    private final EntityConsumerFactoryService entityConsumerFactoryService;
    private final FintCache<String, UserPermission> userPermissionCache;

    public UserPermissionConsumerConfiguration(
            EntityConsumerFactoryService entityConsumerFactoryService,
            FintCache<String, UserPermission> userPermissionCache
    ) {
        this.entityConsumerFactoryService = entityConsumerFactoryService;
        this.userPermissionCache = userPermissionCache;
    }

    @Bean
    @ConditionalOnProperty(value = "fint.flyt.resource-server.user-permissions-consumer.enabled", havingValue = "true")
    ConcurrentMessageListenerContainer<String, UserPermission> createCacheConsumer() {
        return entityConsumerFactoryService.createBatchConsumerFactory(
                UserPermission.class,
                consumerRecords -> consumerRecords
                        .forEach(consumerRecord -> {
                            log.info(
                                    "Consuming userpermission: {} {}",
                                    consumerRecord.key(),
                                    consumerRecord.value().getSourceApplicationIds()
                            );
                            userPermissionCache.put(
                                    consumerRecord.key(),
                                    consumerRecord.value()
                            );
                        })
        ).createContainer(EntityTopicNameParameters.builder().resource("userpermission").build());
    }

}