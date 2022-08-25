package no.fintlabs.resourceserver.security.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

import static no.fintlabs.resourceserver.security.client.ClientAuthorizationUtil.SOURCE_APPLICATION_ID_PREFIX;

@Service
public class ClientJwtConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    private final ClientAuthorizationRequestService clientAuthorizationRequestService;

    public ClientJwtConverter(@Autowired ClientAuthorizationRequestService clientAuthorizationRequestService) {
        this.clientAuthorizationRequestService = clientAuthorizationRequestService;
    }

    @Override
    public Mono<AbstractAuthenticationToken> convert(Jwt source) {
        return Mono.fromCallable(
                () -> Optional.ofNullable(source.<String>getClaim("sub"))
                        .flatMap(clientAuthorizationRequestService::getClientAuthorization)
                        .map(clientAuthorization -> new SimpleGrantedAuthority(
                                SOURCE_APPLICATION_ID_PREFIX + clientAuthorization.getSourceApplicationId()
                        ))
                        .map(grantedAuthority -> new JwtAuthenticationToken(source, List.of(grantedAuthority)))
                        .orElseGet(() -> new JwtAuthenticationToken(source))
        );
    }

}
