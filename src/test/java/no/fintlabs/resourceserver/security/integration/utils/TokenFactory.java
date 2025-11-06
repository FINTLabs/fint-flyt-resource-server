package no.fintlabs.resourceserver.security.integration.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import no.fintlabs.resourceserver.security.integration.utils.testValues.ClientId;
import no.fintlabs.resourceserver.security.integration.utils.testValues.PersonalTokenObjectIdentifier;
import no.fintlabs.resourceserver.security.integration.utils.testValues.PersonalTokenOrgId;
import no.fintlabs.resourceserver.security.user.UserClaim;
import no.fintlabs.resourceserver.security.user.UserRole;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TokenFactory {

    public static TokenWrapper createPersonalToken(
            PersonalTokenOrgId orgId,
            PersonalTokenObjectIdentifier objectIdentifier,
            Set<UserRole> roles
    ) {
        return new TokenWrapper(
                new StringJoiner(", ", "{", "}")
                        .add("orgId: " + orgId)
                        .add("objId: " + objectIdentifier)
                        .add("roles: " + roles)
                        .toString(),
                createJwt(
                        Map.of(
                                UserClaim.ORGANIZATION_ID.getTokenClaimName(), orgId.getClaimValue(),
                                UserClaim.OBJECT_IDENTIFIER.getTokenClaimName(), objectIdentifier.getClaimValue(),
                                UserClaim.ROLES.getTokenClaimName(),
                                roles.stream()
                                        .map(UserRole::getClaimValue)
                                        .collect(Collectors.toSet())
                        )
                )
        );
    }

    public static TokenWrapper createClientToken(ClientId clientId) {
        return new TokenWrapper(
                "{sub: " + clientId + "}",
                createJwt(Map.of(JwtClaimNames.SUB, clientId.getClaimValue()))
        );
    }

    public static Jwt createJwt(Map<String, Object> claims) {
        return new Jwt(
                "testTokenValue",
                Instant.now(),
                Instant.now().plusMillis(20000),
                Map.of("header1", "header1"),
                claims

        );
    }

}
