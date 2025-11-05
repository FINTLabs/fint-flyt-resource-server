package no.fintlabs.resourceserver.security.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserClaim {
    ORGANIZATION_ID("organizationid"),
    OBJECT_IDENTIFIER("objectidentifier"),
    ROLES("roles"),
    SOURCE_APPLICATION_IDS("sourceApplicationIds");

    private final String tokenClaimName;

}
