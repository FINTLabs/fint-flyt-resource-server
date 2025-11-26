package no.novari.flyt.resourceserver.security;

import no.novari.flyt.resourceserver.UrlPaths;
import no.novari.flyt.resourceserver.security.client.internal.InternalClientAuthorityMappingService;
import no.novari.flyt.resourceserver.security.client.internal.InternalClientJwtConverter;
import no.novari.flyt.resourceserver.security.client.sourceapplication.SourceApplicationAuthorityMappingService;
import no.novari.flyt.resourceserver.security.client.sourceapplication.SourceApplicationJwtConverter;
import no.novari.flyt.resourceserver.security.properties.ExternalApiSecurityProperties;
import no.novari.flyt.resourceserver.security.properties.InternalClientApiSecurityProperties;
import no.novari.flyt.resourceserver.security.user.UserJwtConverter;
import no.novari.flyt.resourceserver.security.user.UserRole;
import no.novari.flyt.resourceserver.security.user.UserRoleAuthorityMappingService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.authorization.AuthorityReactiveAuthorizationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@EnableWebFluxSecurity
@AutoConfiguration
public class SecurityConfiguration {

    @Order(0)
    @Bean
    SecurityWebFilterChain actuatorSecurityFilterChain(
            ServerHttpSecurity http,
            SecurityWebFilterChainFactoryService securityWebFilterChainFactoryService
    ) {
        return securityWebFilterChainFactoryService.permitAll(http, "/actuator");
    }

    @Order(1)
    @Bean
    @ConditionalOnBean(InternalUserApiConfiguration.class)
    SecurityWebFilterChain internalAdminApiFilterChain(
            ServerHttpSecurity http,
            UserJwtConverter userJwtConverter,
            UserRoleAuthorityMappingService userRoleAuthorityMappingService,
            SecurityWebFilterChainFactoryService securityWebFilterChainFactoryService
    ) {
        return securityWebFilterChainFactoryService.createFilterChain(
                http,
                UrlPaths.INTERNAL_ADMIN_API,
                userJwtConverter,
                AuthorityReactiveAuthorizationManager.hasAuthority(
                        userRoleAuthorityMappingService.createRoleAuthorityString(UserRole.ADMIN)
                )
        );
    }

    @Order(2)
    @Bean
    @ConditionalOnBean(InternalUserApiConfiguration.class)
    SecurityWebFilterChain internalUserApiFilterChain(
            ServerHttpSecurity http,
            UserJwtConverter userJwtConverter,
            UserRoleAuthorityMappingService userRoleAuthorityMappingService,
            SecurityWebFilterChainFactoryService securityWebFilterChainFactoryService
    ) {
        return securityWebFilterChainFactoryService.createFilterChain(
                http,
                UrlPaths.INTERNAL_API,
                userJwtConverter,
                AuthorityReactiveAuthorizationManager.hasAuthority(
                        userRoleAuthorityMappingService.createRoleAuthorityString(UserRole.USER)
                )
        );
    }

    @Order(1)
    @Bean
    @ConditionalOnMissingBean(InternalUserApiConfiguration.class)
    SecurityWebFilterChain internalApiDisabledFilterChain(
            ServerHttpSecurity http,
            SecurityWebFilterChainFactoryService securityWebFilterChainFactoryService
    ) {
        return securityWebFilterChainFactoryService.denyAll(http, UrlPaths.INTERNAL_API);
    }

    @Order(3)
    @Bean
    @ConditionalOnBean(InternalClientApiConfiguration.class)
    SecurityWebFilterChain internalClientApiFilterChain(
            ServerHttpSecurity http,
            InternalClientApiSecurityProperties internalClientApiSecurityProperties,
            InternalClientJwtConverter internalClientJwtConverter,
            InternalClientAuthorityMappingService internalClientAuthorityMappingService,
            SecurityWebFilterChainFactoryService securityWebFilterChainFactoryService
    ) {
        return securityWebFilterChainFactoryService.createFilterChain(
                http,
                UrlPaths.INTERNAL_CLIENT_API,
                internalClientJwtConverter,
                AuthorityReactiveAuthorizationManager.hasAnyAuthority(
                        internalClientAuthorityMappingService.createInternalClientIdAuthorityStrings(
                                internalClientApiSecurityProperties.getAuthorizedClientIds()
                        ).toArray(new String[0])
                )
        );
    }

    @Order(3)
    @Bean
    @ConditionalOnMissingBean(InternalClientApiConfiguration.class)
    SecurityWebFilterChain internalClientApiDisabledFilterChain(
            ServerHttpSecurity http,
            SecurityWebFilterChainFactoryService securityWebFilterChainFactoryService
    ) {
        return securityWebFilterChainFactoryService.denyAll(http, UrlPaths.INTERNAL_CLIENT_API);
    }

    @Order(4)
    @Bean
    @ConditionalOnBean(ExternalClientApiConfiguration.class)
    SecurityWebFilterChain externalApiFilterChain(
            ServerHttpSecurity http,
            ExternalApiSecurityProperties externalApiSecurityProperties,
            SourceApplicationJwtConverter sourceApplicationJwtConverter,
            SourceApplicationAuthorityMappingService sourceApplicationAuthorityMappingService,
            SecurityWebFilterChainFactoryService securityWebFilterChainFactoryService
    ) {
        return securityWebFilterChainFactoryService.createFilterChain(
                http,
                UrlPaths.EXTERNAL_API,
                sourceApplicationJwtConverter,
                AuthorityReactiveAuthorizationManager.hasAnyAuthority(
                        sourceApplicationAuthorityMappingService.createSourceApplicationAuthorityStrings(
                                externalApiSecurityProperties.getAuthorizedSourceApplicationIds()
                        ).toArray(new String[0])
                )

        );
    }

    @Order(5)
    @Bean
    SecurityWebFilterChain globalFilterChain(
            ServerHttpSecurity http,
            SecurityWebFilterChainFactoryService securityWebFilterChainFactoryService
    ) {
        return securityWebFilterChainFactoryService.denyAll(http);
    }

}
