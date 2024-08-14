package no.fintlabs.resourceserver.security.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.resourceserver.security.properties.InternalApiSecurityProperties;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class UserJwtConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    private final InternalApiSecurityProperties securityProperties;
    private final UserClaimFormattingService userClaimFormattingService;

    public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {
        String organizationId = jwt.getClaimAsString("organizationid");
        String objectIdentifier = jwt.getClaimAsString("objectidentifier");
        List<String> roles = jwt.getClaimAsStringList("roles");
        String adminRole = securityProperties.getAdminRole();

        log.debug("Extracted organization ID from JWT: {}", organizationId);
        log.debug("Extracted roles from JWT: {}", roles);
        log.debug("Extracted objectIdentifier from JWT: {}", objectIdentifier);

        Map<String, Object> modifiedClaims = jwt.getClaims()
                .entrySet()
                .stream()
                .map(entry -> entry.getValue() instanceof String ?
                        new AbstractMap.SimpleEntry<>(
                                entry.getKey(),
                                userClaimFormattingService.removeDoubleQuotesFromClaim((String) entry.getValue())
                        )
                        : entry
                )
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
        String sourceApplicationIdsString = userClaimFormattingService.convertSourceApplicationIdsIntoString(objectIdentifier);
        modifiedClaims.put("sourceApplicationIds", sourceApplicationIdsString);

        Jwt modifiedJwt = Jwt.withTokenValue(jwt.getTokenValue())
                .headers(h -> h.putAll(jwt.getHeaders()))
                .claims(c -> c.putAll(modifiedClaims))
                .build();

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
        return Mono.just(new JwtAuthenticationToken(modifiedJwt, authorities));
    }


}

