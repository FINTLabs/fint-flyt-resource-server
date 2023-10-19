package no.fintlabs.resourceserver.security;

import no.fintlabs.resourceserver.UrlPaths;
import no.fintlabs.resourceserver.security.client.ClientJwtConverter;
import no.fintlabs.resourceserver.security.client.FintFlytJwtUserConverter;
import no.fintlabs.resourceserver.security.client.sourceapplication.SourceApplicationJwtConverter;
import no.fintlabs.resourceserver.security.properties.ApiSecurityProperties;
import no.fintlabs.resourceserver.security.properties.ExternalApiSecurityProperties;
import no.fintlabs.resourceserver.security.properties.InternalApiSecurityProperties;
import no.fintlabs.resourceserver.security.properties.InternalClientApiSecurityProperties;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import reactor.core.publisher.Mono;

@EnableWebFluxSecurity
@EnableAutoConfiguration
public class SecurityConfiguration {

    @Bean
    @ConfigurationProperties("fint.flyt.resource-server.security.api.internal")
    InternalApiSecurityProperties internalApiSecurityProperties() {
        return new InternalApiSecurityProperties();
    }

    @Bean
    @ConfigurationProperties("fint.flyt.resource-server.security.api.internal-client")
    InternalClientApiSecurityProperties internalClientApiSecurityProperties() {
        return new InternalClientApiSecurityProperties();
    }

    @Bean
    @ConfigurationProperties("fint.flyt.resource-server.security.api.external")
    ExternalApiSecurityProperties externalApiSecurityProperties() {
        return new ExternalApiSecurityProperties();
    }

    @Order(1)
    @Bean
    SecurityWebFilterChain internalApiFilterChain(
            ServerHttpSecurity http,
            InternalApiSecurityProperties internalApiSecurityProperties
    ) {
        return createFilterChain(
                http,
                UrlPaths.INTERNAL_API + "/**",
                new FintFlytJwtUserConverter(),
                internalApiSecurityProperties
        );
    }

    @Order(2)
    @Bean
    SecurityWebFilterChain internalClientApiFilterChain(
            ServerHttpSecurity http,
            InternalClientApiSecurityProperties internalClientApiSecurityProperties,
            ClientJwtConverter clientJwtConverter
    ) {
        return createFilterChain(
                http,
                UrlPaths.INTERNAL_CLIENT_API + "/**",
                clientJwtConverter,
                internalClientApiSecurityProperties
        );
    }

    @Order(3)
    @Bean
    SecurityWebFilterChain externalApiFilterChain(
            ServerHttpSecurity http,
            ExternalApiSecurityProperties externalApiSecurityProperties,
            SourceApplicationJwtConverter sourceApplicationJwtConverter
    ) {
        return createFilterChain(
                http,
                UrlPaths.EXTERNAL_API + "/**",
                sourceApplicationJwtConverter,
                externalApiSecurityProperties
        );
    }

    @Order(4)
    @Bean
    SecurityWebFilterChain globalFilterChain(ServerHttpSecurity http) {
        http.addFilterBefore(new AuthorizationLogFilter(), SecurityWebFiltersOrder.AUTHENTICATION);
        return denyAll(http);
    }

    private SecurityWebFilterChain createFilterChain(
            ServerHttpSecurity http,
            String path,
            Converter<Jwt, Mono<AbstractAuthenticationToken>> converter,
            ApiSecurityProperties apiSecurityProperties
    ) {
        http
                .securityMatcher(new PathPatternParserServerWebExchangeMatcher(path))
                .addFilterBefore(new AuthorizationLogFilter(), SecurityWebFiltersOrder.AUTHENTICATION);

        if (!apiSecurityProperties.isEnabled()) {
            return denyAll(http);
        }

        return apiSecurityProperties.isPermitAll()
                ? permitAll(http)
                : http
                .oauth2ResourceServer((resourceServer) -> resourceServer
                        .jwt()
                        .jwtAuthenticationConverter(converter)
                )
                .authorizeExchange()
                .anyExchange()
                .hasAnyAuthority(apiSecurityProperties.getPermittedAuthorities())
                .and()
                .build();
    }

    private SecurityWebFilterChain permitAll(ServerHttpSecurity http) {
        return http
                .authorizeExchange()
                .anyExchange()
                .permitAll()
                .and()
                .build();
    }

    private SecurityWebFilterChain denyAll(ServerHttpSecurity http) {
        return http
                .authorizeExchange()
                .anyExchange()
                .denyAll()
                .and()
                .build();
    }

}
