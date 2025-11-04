package no.fintlabs.resourceserver.security;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.cache.FintCache;
import no.fintlabs.cache.FintCacheConfiguration;
import no.fintlabs.resourceserver.UrlPaths;
import no.fintlabs.resourceserver.security.client.InternalClientJwtConverter;
import no.fintlabs.resourceserver.security.client.sourceapplication.SourceApplicationJwtConverter;
import no.fintlabs.resourceserver.security.properties.ExternalApiSecurityProperties;
import no.fintlabs.resourceserver.security.properties.InternalApiSecurityProperties;
import no.fintlabs.resourceserver.security.properties.InternalClientApiSecurityProperties;
import no.fintlabs.resourceserver.security.user.RoleHierarchyService;
import no.fintlabs.resourceserver.security.user.UserJwtConverter;
import no.fintlabs.resourceserver.security.user.UserRole;
import no.fintlabs.resourceserver.security.user.UserRoleFilteringService;
import no.fintlabs.resourceserver.security.user.userpermission.UserPermission;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authorization.AuthorityReactiveAuthorizationManager;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.UUID;

@EnableWebFluxSecurity
@Import(FintCacheConfiguration.class)
@Slf4j
@Configuration
public class SecurityConfiguration {

    @Bean
    @ConditionalOnProperty("fint.flyt.resource-server.security.api.internal.enabled")
    @ConfigurationProperties("fint.flyt.resource-server.security.api.internal")
    InternalApiSecurityProperties internalApiSecurityProperties() {
        return new InternalApiSecurityProperties();
    }

    @Bean
    @ConditionalOnBean(InternalApiSecurityProperties.class)
    UserRoleFilteringService userRoleFilteringService(InternalApiSecurityProperties internalApiSecurityProperties) {
        return new UserRoleFilteringService(internalApiSecurityProperties);
    }

    @Bean
    @ConditionalOnBean(UserRoleFilteringService.class)
    UserJwtConverter userJwtConverter(
            FintCache<UUID, UserPermission> userPermissionCache,
            UserRoleFilteringService userRoleFilteringService,
            SourceApplicationAuthorityMappingService sourceApplicationAuthorityMappingService,
            RoleHierarchyService roleHierarchyService,
            RoleAuthorityMappingService roleAuthorityMappingService
    ) {
        return new UserJwtConverter(
                userPermissionCache,
                userRoleFilteringService,
                sourceApplicationAuthorityMappingService,
                roleHierarchyService,
                roleAuthorityMappingService
        );
    }

    @Bean
    @ConditionalOnProperty("fint.flyt.resource-server.security.api.internal-client.enabled")
    @ConfigurationProperties("fint.flyt.resource-server.security.api.internal-client")
    InternalClientApiSecurityProperties internalClientApiSecurityProperties() {
        return new InternalClientApiSecurityProperties();
    }

    @Bean
    @ConditionalOnProperty("fint.flyt.resource-server.security.api.external.enabled")
    @ConfigurationProperties("fint.flyt.resource-server.security.api.external")
    ExternalApiSecurityProperties externalApiSecurityProperties() {
        return new ExternalApiSecurityProperties();
    }


    @Order(0)
    @Bean
    SecurityWebFilterChain tenantActuatorSecurityFilterChain(ServerHttpSecurity http, WebFluxProperties props) {
        return http.securityMatcher(matcherFor("/actuator", props))
                .authorizeExchange(spec -> spec.anyExchange().permitAll())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .build();
    }

    @Order(1)
    @Bean
    @ConditionalOnBean(UserJwtConverter.class)
    SecurityWebFilterChain internalAdminApiFilterChain(
            ServerHttpSecurity http,
            WebFluxProperties props,
            UserJwtConverter userJwtConverter,
            RoleAuthorityMappingService roleAuthorityMappingService
    ) {
        return createFilterChain(
                http,
                props,
                UrlPaths.INTERNAL_ADMIN_API,
                userJwtConverter,
                AuthorityReactiveAuthorizationManager.hasAuthority(
                        roleAuthorityMappingService.createRoleAuthorityString(UserRole.ADMIN)
                )
        );
    }

    @Order(2)
    @Bean
    @ConditionalOnBean(InternalApiSecurityProperties.class)
    SecurityWebFilterChain internalApiFilterChain(
            ServerHttpSecurity http,
            WebFluxProperties props,
            UserJwtConverter userJwtConverter,
            RoleAuthorityMappingService roleAuthorityMappingService
    ) {
        return createFilterChain(
                http,
                props,
                UrlPaths.INTERNAL_API,
                userJwtConverter,
                AuthorityReactiveAuthorizationManager.hasAuthority(
                        roleAuthorityMappingService.createRoleAuthorityString(UserRole.USER)
                )
        );
    }

    @Order(3)
    @Bean
    @ConditionalOnBean(InternalClientApiSecurityProperties.class)
    SecurityWebFilterChain internalClientApiFilterChain(
            ServerHttpSecurity http,
            WebFluxProperties props,
            InternalClientApiSecurityProperties internalClientApiSecurityProperties,
            InternalClientJwtConverter internalClientJwtConverter,
            ClientAuthorityMappingService clientAuthorityMappingService
    ) {
        return createFilterChain(
                http,
                props,
                UrlPaths.INTERNAL_CLIENT_API,
                internalClientJwtConverter,
                AuthorityReactiveAuthorizationManager.hasAnyAuthority(
                        clientAuthorityMappingService.createInternalClientIdAuthorityStrings(
                                internalClientApiSecurityProperties.getAuthorizedClientIds()
                        ).toArray(new String[0])
                )
        );
    }

    @Order(4)
    @Bean
    @ConditionalOnBean(ExternalApiSecurityProperties.class)
    SecurityWebFilterChain externalApiFilterChainTest(
            ServerHttpSecurity http,
            WebFluxProperties props,
            ExternalApiSecurityProperties externalApiSecurityProperties,
            SourceApplicationJwtConverter sourceApplicationJwtConverter,
            SourceApplicationAuthorityMappingService sourceApplicationAuthorityMappingService
    ) {
        return createFilterChain(
                http,
                props,
                UrlPaths.EXTERNAL_API,
                sourceApplicationJwtConverter,
                AuthorityReactiveAuthorizationManager.hasAnyAuthority(
                        sourceApplicationAuthorityMappingService.createSourceApplicationAuthorityStrings(
                                externalApiSecurityProperties.getAuthorizedClientIds()
                        ).toArray(new String[0])
                )

        );
    }

    @Order(5)
    @Bean
    SecurityWebFilterChain globalFilterChain(ServerHttpSecurity http) {
        http.addFilterBefore(new AuthorizationLogFilter(), SecurityWebFiltersOrder.AUTHENTICATION);
        return http
                .authorizeExchange(exchange -> exchange.anyExchange().denyAll())
                .build();
    }

    // TODO 04/11/2025 eivindmorch: Remove?
    private ServerWebExchangeMatcher matcherFor(String path, WebFluxProperties props) {
        String normalized = path.startsWith("/") ? path : "/" + path;
        ServerWebExchangeMatcher plain = new PathPatternParserServerWebExchangeMatcher(normalized + "/**");

        String base = props.getBasePath();
        if (!StringUtils.hasText(base)) {
            return plain;
        }

        String prefixed = StringUtils.trimTrailingCharacter(
                (base.startsWith("/") ? base : "/" + base), '/'
        ) + normalized + "/**";

        return new OrServerWebExchangeMatcher(
                plain,
                new PathPatternParserServerWebExchangeMatcher(prefixed)
        );
    }

    private SecurityWebFilterChain createFilterChain(ServerHttpSecurity http,
                                                     WebFluxProperties props,
                                                     String path,
                                                     Converter<Jwt, Mono<AbstractAuthenticationToken>> converter,
                                                     ReactiveAuthorizationManager<AuthorizationContext> manager) {
        http.securityMatcher(matcherFor(path, props))
                .addFilterBefore(new AuthorizationLogFilter(), SecurityWebFiltersOrder.AUTHENTICATION);
        return http.oauth2ResourceServer(rs -> rs.jwt(jwt -> jwt.jwtAuthenticationConverter(converter)))
                .authorizeExchange(exchange -> exchange.anyExchange().access(manager))
                .build();
    }

}
