package no.fintlabs.resourceserver.security.integration.utils.testValues;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PersonalTokenOrgId {
    WITH_ALL_USER_ACCESS("domain-with-all-user-access.no"),
    WITH_ADMIN_AND_DEV_ACCESS("domain-with-admin-and-dev-access.no"),
    WITH_NO_ACCESS("domain-without-role-access.no");
    private final String claimValue;
}
