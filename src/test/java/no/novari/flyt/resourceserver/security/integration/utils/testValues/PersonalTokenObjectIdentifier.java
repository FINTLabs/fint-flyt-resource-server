package no.novari.flyt.resourceserver.security.integration.utils.testValues;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public enum PersonalTokenObjectIdentifier {
    WITH_SA_1_2_AUTHORIZATIONS(UUID.fromString("a3be307e-e8d4-4475-8ed0-8d948dc47b86")),
    WITH_NO_SA_AUTHORIZATIONS(UUID.fromString("2682892c-0424-4f47-8749-9eeb98fec06f")),
    WITH_NO_USER_PERMISSION(UUID.fromString("1e6d874d-b76e-4cfc-b98e-0b83cb5d53df"));
    private final UUID uuid;

    public String getClaimValue() {
        return uuid.toString();
    }
}
