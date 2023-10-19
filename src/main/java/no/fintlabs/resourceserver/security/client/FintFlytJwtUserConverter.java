package no.fintlabs.resourceserver.security.client;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class FintFlytJwtUserConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    private final HashMap<String, String> authoritiesMap = new HashMap<>() {
        {
            this.put("scope", "SCOPE_");
        }
    };

    public FintFlytJwtUserConverter() {
        this.addMapping("organizationid", "ORGID_");
        this.addMapping("organizationnumber", "ORGNR_");
        this.addMapping("roles", "ROLE_");
    }

    public void addMapping(String claimName, String prefix) {
        this.authoritiesMap.put(claimName, prefix);
    }

    private Flux<GrantedAuthority> extractAuthorities(Jwt jwt, String claimName, String prefix) {
        Object claim = jwt.getClaim(claimName);

        return Flux.fromIterable(getAuthoritiesFromClaim(claim))
                .map(authority -> new SimpleGrantedAuthority(prefix + authority));
    }

    private Iterable<String> getAuthoritiesFromClaim(Object claim) {
        if (claim instanceof String) {
            return Arrays.asList(((String) claim).split(" "));
        } else if (claim instanceof Collection) {
            return (Collection<String>) claim;
        } else {
            return Collections.emptyList();
        }
    }

    public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {
        return Flux.fromIterable(this.authoritiesMap.entrySet())
                .filter((claimPrefixEntry) -> jwt.hasClaim(claimPrefixEntry.getKey()))
                .flatMap((entry) -> this.extractAuthorities(jwt, entry.getKey(), entry.getValue()))
                .map(this::modifyAuthority)
                .collectList()
                .map((authorities) -> new JwtAuthenticationToken(jwt, authorities));
    }

    private GrantedAuthority modifyAuthority(GrantedAuthority auth) {
        return new SimpleGrantedAuthority(auth.getAuthority().replace("\\", "").replace("\"", ""));
    }

}

