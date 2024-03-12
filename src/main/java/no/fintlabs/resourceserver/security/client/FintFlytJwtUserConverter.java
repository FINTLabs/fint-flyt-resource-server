package no.fintlabs.resourceserver.security.client;

import lombok.extern.slf4j.Slf4j;
import no.fintlabs.resourceserver.security.properties.InternalApiSecurityProperties;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class FintFlytJwtUserConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    private final InternalApiSecurityProperties securityProperties;

    public FintFlytJwtUserConverter(InternalApiSecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {

        String organizationId = jwt.getClaimAsString("organizationid");
        List<String> roles = jwt.getClaimAsStringList("roles");
        String adminRole = securityProperties.getAdminRole();

        log.debug("Extracted organization ID from JWT: {}", organizationId);
        log.debug("Extracted roles from JWT: {}", roles);

        List<GrantedAuthority> authorities = new ArrayList<>();

        if (organizationId != null && roles != null) {

            if (adminRole != null && !adminRole.isBlank()) {
                boolean isAdmin = roles.contains(adminRole);

                if (isAdmin) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                }
            }

            for (String role : roles) {
                authorities.add(new SimpleGrantedAuthority("ORGID_" + organizationId + "_ROLE_" + role));
            }
        }

        Map<String, Object> modifiedClaims = new HashMap<>();

        jwt.getClaims().forEach((key, value) -> modifiedClaims.put(key, modifyClaim(value)));

        Jwt modifiedJwt = Jwt.withTokenValue(jwt.getTokenValue())
                .headers(h -> h.putAll(jwt.getHeaders()))
                .claims(c -> c.putAll(modifiedClaims))
                .build();

        return Mono.just(new JwtAuthenticationToken(modifiedJwt, authorities));
    }

    private Object modifyClaim(Object claim) {
        if (claim instanceof String) {
            return ((String) claim).replace("\\", "").replace("\"", "");
        }
        return claim;
    }

}

