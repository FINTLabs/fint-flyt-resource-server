package no.fintlabs.resourceserver.security.userpermission;

import no.fintlabs.cache.FintCache;
import no.fintlabs.kafka.entity.EntityConsumerFactoryService;
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters;
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
    ConcurrentMessageListenerContainer<String, UserPermission> createCacheConsumer() {
        return entityConsumerFactoryService.createBatchConsumerFactory(
                UserPermission.class,
                consumerRecords -> consumerRecords
                        .forEach(consumerRecord -> userPermissionCache.put(
                                consumerRecord.key(),
                                consumerRecord.value()
                        ))
        ).createContainer(EntityTopicNameParameters.builder().resource("userpermission").build());
    }

}