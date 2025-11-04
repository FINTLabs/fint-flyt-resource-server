package no.fintlabs.resourceserver.security.client;

import no.fintlabs.resourceserver.security.ClientAuthorityMappingService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Service
public class InternalClientJwtConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    private final ClientAuthorityMappingService clientAuthorityMappingService;

    public InternalClientJwtConverter(ClientAuthorityMappingService clientAuthorityMappingService) {
        this.clientAuthorityMappingService = clientAuthorityMappingService;
    }

    @Override
    public Mono<AbstractAuthenticationToken> convert(Jwt source) {
        return Mono.fromCallable(
                () -> Optional.ofNullable(source.getSubject())
                        .map(clientAuthorityMappingService::createInternalClientIdAuthority)
                        .map(grantedAuthority -> new JwtAuthenticationToken(source, List.of(grantedAuthority)))
                        .orElseGet(() -> new JwtAuthenticationToken(source))
        );
    }

}
