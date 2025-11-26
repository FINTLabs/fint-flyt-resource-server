package no.novari.flyt.resourceserver.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class SecurityWebFilterChainFactoryService {

    public SecurityWebFilterChain createFilterChain(
            ServerHttpSecurity http,
            String path,
            Converter<Jwt, Mono<AbstractAuthenticationToken>> converter,
            ReactiveAuthorizationManager<AuthorizationContext> manager
    ) {
        return addCommonConfig(http)
                .securityMatcher(new PathPatternParserServerWebExchangeMatcher(path + "/**"))
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(jwtSpec -> jwtSpec.jwtAuthenticationConverter(converter))
                )
                .authorizeExchange(exchange -> exchange.anyExchange().access(manager))
                .build();
    }

    public SecurityWebFilterChain permitAll(ServerHttpSecurity http, String path) {
        return addCommonConfig(http)
                .securityMatcher(new PathPatternParserServerWebExchangeMatcher(path + "/**"))
                .authorizeExchange(spec -> spec.anyExchange().permitAll())
                .build();
    }

    public SecurityWebFilterChain denyAll(ServerHttpSecurity http, String path) {
        return denyAll(http.securityMatcher(new PathPatternParserServerWebExchangeMatcher(path + "/**")));
    }

    public SecurityWebFilterChain denyAll(ServerHttpSecurity http) {
        return addCommonConfig(http)
                .authorizeExchange(exchange -> exchange.anyExchange().denyAll())
                .build();
    }

    private ServerHttpSecurity addCommonConfig(ServerHttpSecurity http) {
        return http
                .addFilterBefore(new AuthorizationLogFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable);
    }

}
