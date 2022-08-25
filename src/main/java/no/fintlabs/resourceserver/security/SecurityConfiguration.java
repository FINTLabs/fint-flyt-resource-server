package no.fintlabs.resourceserver.security;

import no.fintlabs.UrlPaths;
import no.fintlabs.resourceserver.security.client.ClientJwtConverter;
import no.fintlabs.resourceserver.security.properties.ExternalApiSecurityProperties;
import no.fintlabs.resourceserver.security.properties.InternalApiSecurityProperties;
import no.vigoiks.resourceserver.security.FintJwtUserConverter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;

import java.util.List;

import static no.fintlabs.resourceserver.security.client.ClientAuthorizationUtil.SOURCE_APPLICATION_ID_PREFIX;

@EnableWebFluxSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration {

    private final ClientJwtConverter clientJwtConverter;

    public SecurityConfiguration(ClientJwtConverter clientJwtConverter) {
        this.clientJwtConverter = clientJwtConverter;
    }


    @Bean
    @ConfigurationProperties("fint.flyt.resource-server.security.api.internal")
    InternalApiSecurityProperties internalApiSecurityProperties() {
        return new InternalApiSecurityProperties();
    }

//    @Bean
//    @ConfigurationProperties("fint.flyt.resource-server.security.api.admin")
//    AdminApiSecurityProperties adminApiSecurityProperties() {
//        return new AdminApiSecurityProperties();
//    }

    @Bean
    @ConfigurationProperties("fint.flyt.resource-server.security.api.external")
    ExternalApiSecurityProperties externalApiSecurityProperties() {
        return new ExternalApiSecurityProperties();
    }

    @Order(1)
    @Bean
    SecurityWebFilterChain internalApiFilterChain(
            InternalApiSecurityProperties internalApiSecurityProperties,
            ServerHttpSecurity http
    ) {
        http
                .securityMatcher(new PathPatternParserServerWebExchangeMatcher(UrlPaths.INTERNAL_API + "/**"))
                .addFilterBefore(new AuthorizationLogFilter(), SecurityWebFiltersOrder.AUTHENTICATION);

        if (!internalApiSecurityProperties.isEnabled()) {
            return denyAll(http);
        }

        return internalApiSecurityProperties.isPermitAll()
                ? permitAll(http)
                : http
                .oauth2ResourceServer((resourceServer) -> resourceServer
                        .jwt()
                        .jwtAuthenticationConverter(new FintJwtUserConverter())
                )
                .authorizeExchange()
                .anyExchange()
                .hasAnyAuthority(mapToAuthoritiesArray("ORGID_", internalApiSecurityProperties.getAuthorizedOrgIds()))
                .and()
                .build();
    }

//    @Order(2)
//    @Bean
//    SecurityWebFilterChain adminApiFilterChain(
//            AdminApiSecurityProperties adminApiSecurityProperties,
//            ServerHttpSecurity http
//    ) {
//        http
//                .securityMatcher(new PathPatternParserServerWebExchangeMatcher(UrlPaths.ADMIN_API + "/**"))
//                .addFilterBefore(new AuthorizationLogFilter(), SecurityWebFiltersOrder.AUTHENTICATION);
//
//        if (adminApiSecurityProperties.isPermitAll()) {
//            return permitAll(http);
//        }
//
//        return http
//                .oauth2ResourceServer((resourceServer) -> resourceServer
//                        .jwt()
//                        .jwtAuthenticationConverter(new FintJwtUserConverter())
//                )
//                .authorizeExchange()
//                .anyExchange()
//                .access((Mono<Authentication> authenticationMono, AuthorizationContext object) -> authenticationMono.map(authentication -> {
//                            Set<String> authorizedOrgIdAuthorities = adminApiSecurityProperties.getAuthorizedOrgIds()
//                                    .stream()
//                                    .map(orgId -> "ORGID_" + orgId)
//                                    .collect(Collectors.toSet());
//
//                            Set<String> grantedAuthorities = authentication
//                                    .getAuthorities()
//                                    .stream()
//                                    .map(GrantedAuthority::getAuthority)
//                                    .collect(Collectors.toSet());
//
//                            return !Collections.disjoint(grantedAuthorities, authorizedOrgIdAuthorities)
//                                    && !(adminApiSecurityProperties.isRequireAdminRole() && !grantedAuthorities.contains("ROLE_admin"));
//                        }
//                ).map(AuthorizationDecision::new))
//                .and()
//                .build();
//    }

    @Order(3)
    @Bean
    SecurityWebFilterChain externalApiFilterChain(
            ExternalApiSecurityProperties externalApiSecurityProperties,
            ServerHttpSecurity http
    ) {
        http
                .securityMatcher(new PathPatternParserServerWebExchangeMatcher(UrlPaths.EXTERNAL_API + "/**"))
                .addFilterBefore(new AuthorizationLogFilter(), SecurityWebFiltersOrder.AUTHENTICATION);

        if (!externalApiSecurityProperties.isEnabled()) {
            return denyAll(http);
        }

        return externalApiSecurityProperties.isPermitAll()
                ? permitAll(http)
                : http
                .oauth2ResourceServer((resourceServer) -> resourceServer
                        .jwt()
                        .jwtAuthenticationConverter(clientJwtConverter)
                )
                .authorizeExchange()
                .anyExchange()
                .hasAnyAuthority(mapToAuthoritiesArray(
                        SOURCE_APPLICATION_ID_PREFIX,
                        externalApiSecurityProperties.getAuthorizedClientIds()
                ))
                .and()
                .build();
    }

    @Order(4)
    @Bean
    SecurityWebFilterChain globalFilterChain(
            ServerHttpSecurity http
    ) {
        http.addFilterBefore(new AuthorizationLogFilter(), SecurityWebFiltersOrder.AUTHENTICATION);
        return denyAll(http);
    }

    private SecurityWebFilterChain permitAll(ServerHttpSecurity http) {
        return http
//                .csrf().disable() // TODO: 22/08/2022 Enable/disable?
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

    private String[] mapToAuthoritiesArray(String prefix, List<String> values) {
        return values
                .stream()
                .map(id -> prefix + id)
                .toArray(String[]::new);
    }

}
