package no.fintlabs.resourceserver.security.client.sourceapplication;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Service
public class SourceApplicationJwtConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    private final SourceApplicationAuthorizationService sourceApplicationAuthorizationService;
    private final SourceApplicationAuthorizationRequestService sourceApplicationAuthorizationRequestService;

    public SourceApplicationJwtConverter(
            SourceApplicationAuthorizationService sourceApplicationAuthorizationService,
            SourceApplicationAuthorizationRequestService sourceApplicationAuthorizationRequestService
    ) {
        this.sourceApplicationAuthorizationService = sourceApplicationAuthorizationService;
        this.sourceApplicationAuthorizationRequestService = sourceApplicationAuthorizationRequestService;
    }

    @Override
    public Mono<AbstractAuthenticationToken> convert(Jwt source) {
        return Mono.fromCallable(
                () -> Optional.ofNullable(source.<String>getClaim("sub"))
                        .flatMap(sourceApplicationAuthorizationRequestService::getClientAuthorization)
                        .map(sourceApplicationAuthorizationService::getAuthority)
                        .map(grantedAuthority -> new JwtAuthenticationToken(source, List.of(grantedAuthority)))
                        .orElseGet(() -> new JwtAuthenticationToken(source))
        );
    }

}
