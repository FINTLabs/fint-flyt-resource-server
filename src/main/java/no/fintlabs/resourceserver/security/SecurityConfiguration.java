package no.fintlabs.resourceserver.security;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.cache.FintCacheConfiguration;
import no.fintlabs.resourceserver.UrlPaths;
import no.fintlabs.resourceserver.security.client.InternalClientJwtConverter;
import no.fintlabs.resourceserver.security.client.sourceapplication.SourceApplicationJwtConverter;
import no.fintlabs.resourceserver.security.properties.ExternalApiSecurityProperties;
import no.fintlabs.resourceserver.security.properties.InternalApiSecurityProperties;
import no.fintlabs.resourceserver.security.properties.InternalClientApiSecurityProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
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
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import reactor.core.publisher.Mono;

@EnableWebFluxSecurity
@Import(FintCacheConfiguration.class)
@Slf4j
public class SecurityConfiguration {

    @Bean
    @ConditionalOnProperty("fint.flyt.resource-server.security.api.internal.enabled")
    @ConfigurationProperties("fint.flyt.resource-server.security.api.internal")
    InternalApiSecurityProperties internalApiSecurityProperties() {
        return new InternalApiSecurityProperties();
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

    // TODO 16/10/2025 eivindmorch: Wrong path? Is this necessary at all?
    @Order(0)
    @Bean
    SecurityWebFilterChain actuatorSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .securityMatcher(new PathPatternParserServerWebExchangeMatcher("/actuator/**"))
                .authorizeExchange(exchange -> exchange.anyExchange().permitAll())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .build();
    }

//    @Order(1)
//    @Bean
//    @ConditionalOnBean(InternalApiSecurityProperties.class)
//    SecurityWebFilterChain internalApiFilterChain(
//            ServerHttpSecurity http,
//            InternalApiSecurityProperties internalApiSecurityProperties,
//            UserJwtConverter userJwtConverter,
//            AuthorityMappingService authorityMappingService
//    ) {
//        //log.debug("Internal API Security Properties: {}", (Object) internalApiSecurityProperties.getPermittedAuthorities());
//        return createFilterChain(
//                http,
//                UrlPaths.INTERNAL_API + "/**",
//                userJwtConverter,
//                (authenticationMono, authorizationContext) ->
//                        authenticationMono.map(authentication -> {
//                            authentication.getAuthorities().stream()
//                        })
    // TODO 21/10/2025 eivindmorch: Should check orgId and user role in converter when creating authentication, not in filter chain?
//                AuthorityReactiveAuthorizationManager
//                        .hasAnyAuthority(
//                                mapToAuthoritiesArray(
//                                        internalApiSecurityProperties.getUserRolesPerOrgId()
//                                                .entrySet()
//                                                .stream()
//                                                .flatMap(entry ->
//                                                        authorityMappingService.createOrgAndRoleAuthorities(
//                                                                entry.getKey(),
//                                                                entry.getValue()
//                                                        ).stream()
//                                                ).to
//                                )
//                        )
//        );
//    }

    @Order(2)
    @Bean
    @ConditionalOnBean(InternalClientApiSecurityProperties.class)
    SecurityWebFilterChain internalClientApiFilterChain(
            ServerHttpSecurity http,
            InternalClientApiSecurityProperties internalClientApiSecurityProperties,
            InternalClientJwtConverter internalClientJwtConverter,
            AuthorityMappingService authorityMappingService
    ) {
        return createFilterChain(
                http,
                UrlPaths.INTERNAL_CLIENT_API + "/**",
                internalClientJwtConverter,
                AuthorityReactiveAuthorizationManager.hasAnyAuthority(
                        authorityMappingService.toAuthoritiesStringArray(
                                AuthorityPrefix.SOURCE_APPLICATION_ID,
                                internalClientApiSecurityProperties.getAuthorizedClientIds()
                        )
                )
        );
    }

    @Order(3)
    @Bean
    @ConditionalOnBean(ExternalApiSecurityProperties.class)
    SecurityWebFilterChain externalApiFilterChain(
            ServerHttpSecurity http,
            ExternalApiSecurityProperties externalApiSecurityProperties,
            SourceApplicationJwtConverter sourceApplicationJwtConverter,
            AuthorityMappingService authorityMappingService
    ) {
        return createFilterChain(
                http,
                UrlPaths.EXTERNAL_API + "/**",
                sourceApplicationJwtConverter,
                AuthorityReactiveAuthorizationManager.hasAnyAuthority(
                        authorityMappingService.toAuthoritiesStringArray(
                                AuthorityPrefix.CLIENT_ID,
                                externalApiSecurityProperties.getAuthorizedClientIds()
                        )
                )
        );
    }

    // TODO 16/10/2025 eivindmorch: Necessary?
    @Order(4)
    @Bean
    SecurityWebFilterChain globalFilterChain(ServerHttpSecurity http) {
        http.addFilterBefore(new AuthorizationLogFilter(), SecurityWebFiltersOrder.AUTHENTICATION);
        return http
                .authorizeExchange(exchange -> exchange.anyExchange().denyAll())
                .build();
    }

    private SecurityWebFilterChain createFilterChain(
            ServerHttpSecurity http,
            String path,
            Converter<Jwt, Mono<AbstractAuthenticationToken>> converter,
            ReactiveAuthorizationManager<AuthorizationContext> manager
    ) {
        http
                .securityMatcher(new PathPatternParserServerWebExchangeMatcher(path))
                .addFilterBefore(new AuthorizationLogFilter(), SecurityWebFiltersOrder.AUTHENTICATION);

        return http
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(jwtSpec -> jwtSpec.jwtAuthenticationConverter(converter))
                )
                .authorizeExchange(exchange -> exchange.anyExchange().access(manager))
                .build();
    }

}
