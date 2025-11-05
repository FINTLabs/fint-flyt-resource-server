package no.fintlabs.resourceserver.integration.utils.testValues;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PersonalTokenOrgId {
    WITH_USER_ACCESS("domain-with-user-access.no"),
    WITH_ADMIN_AND_DEV_ACCESS("domain-with-admin-and-dev-access.no.no"),
    WITH_NO_ACCESS("domain-without-role-access.no");
    private final String claimValue;
}
