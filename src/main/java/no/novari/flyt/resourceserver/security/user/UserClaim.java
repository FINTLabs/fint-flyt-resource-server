package no.novari.flyt.resourceserver.security.user;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum UserClaim {
    ORGANIZATION_ID("organizationid"),
    OBJECT_IDENTIFIER("objectidentifier"),
    ROLES("roles"),
    SOURCE_APPLICATION_IDS("sourceApplicationIds");

    private final String tokenClaimName;

}
