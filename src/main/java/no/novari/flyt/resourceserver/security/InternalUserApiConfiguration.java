package no.novari.flyt.resourceserver.security;

import no.fintlabs.cache.FintCache;
import no.fintlabs.cache.FintCacheConfiguration;
import no.fintlabs.cache.FintCacheManager;
import no.fintlabs.cache.FintCacheOptions;
import no.fintlabs.kafka.consuming.ErrorHandlerFactory;
import no.fintlabs.kafka.consuming.ParameterizedListenerContainerFactoryService;
import no.novari.flyt.resourceserver.security.client.sourceapplication.SourceApplicationAuthorityMappingService;
import no.novari.flyt.resourceserver.security.properties.InternalApiSecurityProperties;
import no.novari.flyt.resourceserver.security.user.UserJwtConverter;
import no.novari.flyt.resourceserver.security.user.UserRoleAuthorityMappingService;
import no.novari.flyt.resourceserver.security.user.UserRoleFilteringService;
import no.novari.flyt.resourceserver.security.user.UserRoleHierarchyService;
import no.novari.flyt.resourceserver.security.user.permission.UserPermission;
import no.novari.flyt.resourceserver.security.user.permission.UserPermissionCachingListenerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import java.time.Duration;
import java.util.UUID;

@AutoConfiguration
@ConditionalOnProperty(
        prefix = "novari.flyt.resource-server.security.api",
        value = "internal.enabled",
        havingValue = "true"
)
@Import(FintCacheConfiguration.class)
public class InternalUserApiConfiguration {

    @Bean
    @ConfigurationProperties("novari.flyt.resource-server.security.api.internal")
    InternalApiSecurityProperties internalApiSecurityProperties() {
        return new InternalApiSecurityProperties();
    }

    @Bean
    UserRoleFilteringService userRoleFilteringService(InternalApiSecurityProperties internalApiSecurityProperties) {
        return new UserRoleFilteringService(internalApiSecurityProperties);
    }

    @Bean
    FintCache<UUID, UserPermission> userPermissionCache(FintCacheManager fintCacheManager) {
        return fintCacheManager.createCache(
                "userpermission",
                UUID.class,
                UserPermission.class,
                FintCacheOptions
                        .builder()
                        .timeToLive(Duration.ofMillis(9223372036854775807L))
                        .heapSize(1000000L)
                        .build()
        );
    }

    @Bean
    UserRoleAuthorityMappingService userRoleAuthorityMappingService(AuthorityMappingService authorityMappingService) {
        return new UserRoleAuthorityMappingService(authorityMappingService);
    }

    @Bean
    UserRoleHierarchyService userRoleHierarchyService() {
        return new UserRoleHierarchyService();
    }


    @Bean
    UserJwtConverter userJwtConverter(
            FintCache<UUID, UserPermission> userPermissionCache,
            UserRoleFilteringService userRoleFilteringService,
            SourceApplicationAuthorityMappingService sourceApplicationAuthorityMappingService,
            UserRoleHierarchyService userRoleHierarchyService,
            UserRoleAuthorityMappingService userRoleAuthorityMappingService
    ) {
        return new UserJwtConverter(
                userPermissionCache,
                userRoleFilteringService,
                sourceApplicationAuthorityMappingService,
                userRoleHierarchyService,
                userRoleAuthorityMappingService
        );
    }

    @Bean("userPermissionCachingListener")
    ConcurrentMessageListenerContainer<String, UserPermission> userPermissionCachingListener(
            ParameterizedListenerContainerFactoryService containerFactoryService,
            FintCache<UUID, UserPermission> userPermissionCache,
            ErrorHandlerFactory errorHandlerFactory
    ) {
        return new UserPermissionCachingListenerFactory().create(
                containerFactoryService,
                userPermissionCache,
                errorHandlerFactory
        );
    }

}
