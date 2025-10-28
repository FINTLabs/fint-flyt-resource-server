package no.fintlabs.resourceserver.security;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.cache.FintCacheConfiguration;
import no.fintlabs.resourceserver.UrlPaths;
import no.fintlabs.resourceserver.security.client.InternalClientJwtConverter;
import no.fintlabs.resourceserver.security.client.sourceapplication.SourceApplicationJwtConverter;
import no.fintlabs.resourceserver.security.properties.ExternalApiSecurityProperties;
import no.fintlabs.resourceserver.security.properties.InternalApiSecurityProperties;
import no.fintlabs.resourceserver.security.properties.InternalClientApiSecurityProperties;
import no.fintlabs.resourceserver.security.user.UserJwtConverter;
import no.fintlabs.resourceserver.security.user.UserRole;
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

import java.util.Map;
import java.util.Set;

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

    private static final Map<UserRole, Set<UserRole>> ROLE_HIERARCHY = Map.of(
            UserRole.USER, Set.of(UserRole.USER, UserRole.DEVELOPER, UserRole.ADMIN),
            UserRole.DEVELOPER, Set.of(UserRole.DEVELOPER),
            UserRole.ADMIN, Set.of(UserRole.ADMIN, UserRole.DEVELOPER)
    );

    @Order(0)
    @Bean
    SecurityWebFilterChain actuatorSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .securityMatcher(new PathPatternParserServerWebExchangeMatcher("/actuator/**"))
                .authorizeExchange(exchange -> exchange.anyExchange().permitAll())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .build();
    }

    @Order(1)
    @Bean
    @ConditionalOnBean(InternalApiSecurityProperties.class)
    SecurityWebFilterChain internalAdminApiFilterChain(
            ServerHttpSecurity http,
            UserJwtConverter userJwtConverter,
            RoleAuthorityMappingService roleAuthorityMappingService
    ) {
        return createFilterChain(
                http,
                UrlPaths.INTERNAL_ADMIN_API + "/**",
                userJwtConverter,
                AuthorityReactiveAuthorizationManager.hasAnyAuthority(
                        roleAuthorityMappingService.createRoleAuthorityStrings(ROLE_HIERARCHY.get(UserRole.ADMIN))
                                .toArray(new String[0])
                )
        );
    }

    @Order(2)
    @Bean
    @ConditionalOnBean(InternalApiSecurityProperties.class)
    SecurityWebFilterChain internalApiFilterChain(
            ServerHttpSecurity http,
            UserJwtConverter userJwtConverter,
            RoleAuthorityMappingService roleAuthorityMappingService
    ) {
        return createFilterChain(
                http,
                UrlPaths.INTERNAL_API + "/**",
                userJwtConverter,
                AuthorityReactiveAuthorizationManager.hasAnyAuthority(
                        roleAuthorityMappingService.createRoleAuthorityStrings(ROLE_HIERARCHY.get(UserRole.USER)).toArray(new String[0])
                )
        );
    }

    @Order(3)
    @Bean
    @ConditionalOnBean(InternalClientApiSecurityProperties.class)
    SecurityWebFilterChain internalClientApiFilterChain(
            ServerHttpSecurity http,
            InternalClientApiSecurityProperties internalClientApiSecurityProperties,
            InternalClientJwtConverter internalClientJwtConverter,
            ClientAuthorityMappingService clientAuthorityMappingService

    ) {
        return createFilterChain(
                http,
                UrlPaths.INTERNAL_CLIENT_API + "/**",
                internalClientJwtConverter,
                AuthorityReactiveAuthorizationManager.hasAnyAuthority(
                        clientAuthorityMappingService.createInternalClientIdAuthorityStrings(
                                internalClientApiSecurityProperties.getAuthorizedClientIds()
                        ).toArray(new String[0])
                )
        );
    }

    // TODO:
    //  INTERNAL CLIENT
    //  – Token med clientId (183812y7398172389)
    //  – Setter authority CLIENT_ID_183812y7398172389
    //  – Validerer at authorizedClientId i properties i applikasjonen er satt til 183812y7398172389
    //  EXTERNAL CLIENT
    //  – Token med clientId (183812y7398172389)
    //  – Sender clientId til authorization-service og får tilbake evt source application id (2)
    //  – Setter source application id (2) som authority (SOURCE_APPLICATION_ID_2)
    //  – Validerer at authorizedClientId (bytt navn) i properties i applikasjonen er satt til 2


    @Order(4)
    @Bean
    @ConditionalOnBean(ExternalApiSecurityProperties.class)
    SecurityWebFilterChain externalApiFilterChainTest(
            ServerHttpSecurity http,
            ExternalApiSecurityProperties externalApiSecurityProperties,
            SourceApplicationJwtConverter sourceApplicationJwtConverter,
            SourceApplicationAuthorityMappingService sourceApplicationAuthorityMappingService
    ) {
        return createFilterChain(
                http,
                UrlPaths.EXTERNAL_API + "/**",
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
