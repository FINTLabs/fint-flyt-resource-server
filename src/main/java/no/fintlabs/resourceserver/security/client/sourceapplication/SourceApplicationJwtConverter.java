package no.fintlabs.resourceserver.security.client.sourceapplication;

import no.fintlabs.resourceserver.security.SourceApplicationAuthorityMappingService;
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

    private final SourceApplicationAuthorizationRequestService sourceApplicationAuthorizationRequestService;
    private final SourceApplicationAuthorityMappingService sourceApplicationAuthorityMappingService;

    public SourceApplicationJwtConverter(
            SourceApplicationAuthorizationRequestService sourceApplicationAuthorizationRequestService,
            SourceApplicationAuthorityMappingService sourceApplicationAuthorityMappingService
    ) {
        this.sourceApplicationAuthorizationRequestService = sourceApplicationAuthorizationRequestService;
        this.sourceApplicationAuthorityMappingService = sourceApplicationAuthorityMappingService;
    }

    @Override
    public Mono<AbstractAuthenticationToken> convert(Jwt source) {
        return Mono.fromCallable(
                () -> Optional.ofNullable(source.getSubject())
                        .flatMap(sourceApplicationAuthorizationRequestService::getClientAuthorization)
                        .map(SourceApplicationAuthorization::getSourceApplicationId)
                        .map(sourceApplicationAuthorityMappingService::createSourceApplicationAuthority)
                        .map(grantedAuthority -> new JwtAuthenticationToken(source, List.of(grantedAuthority)))
                        .orElseGet(() -> new JwtAuthenticationToken(source))
        );
    }

}
