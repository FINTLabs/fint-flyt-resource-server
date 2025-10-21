package no.fintlabs.resourceserver.security.client;

import no.fintlabs.resourceserver.security.AuthorityMappingService;
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

    private final AuthorityMappingService authorityMappingService;

    public InternalClientJwtConverter(AuthorityMappingService authorityMappingService) {
        this.authorityMappingService = authorityMappingService;
    }

    @Override
    public Mono<AbstractAuthenticationToken> convert(Jwt source) {
        return Mono.fromCallable(
                () -> Optional.ofNullable(source.<String>getClaim("sub"))
                        .map(authorityMappingService::createInternalClientIdAuthority)
                        .map(grantedAuthority -> new JwtAuthenticationToken(source, List.of(grantedAuthority)))
                        .orElseGet(() -> new JwtAuthenticationToken(source))
        );
    }

}
