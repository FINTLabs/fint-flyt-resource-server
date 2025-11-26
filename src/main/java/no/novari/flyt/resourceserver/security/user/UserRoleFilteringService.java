package no.novari.flyt.resourceserver.security.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.novari.flyt.resourceserver.security.properties.InternalApiSecurityProperties;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class UserRoleFilteringService {

    private final InternalApiSecurityProperties internalApiSecurityProperties;

    public Set<UserRole> filter(Collection<String> roleValues, String organizationId) {

        log.debug("roleValues : {}", roleValues);

        if (roleValues.isEmpty()) {
            return Collections.emptySet();
        }

        Set<UserRole> filteredUserRoles = roleValues
                .stream()
                .map(UserRole::getUserRoleFromValue)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        log.debug("filteredUserRoles before filter : {}", filteredUserRoles);

        if (filteredUserRoles.isEmpty()) {
            return Collections.emptySet();
        }

        Set<UserRole> roleFilter = internalApiSecurityProperties.getUserRoleFilterPerOrgId()
                .getOrDefault(organizationId, Collections.emptySet());

        log.debug("roleFilter: {}", roleFilter);

        filteredUserRoles.retainAll(roleFilter);

        log.debug("filteredUserRoles after filter : {}", filteredUserRoles);

        return filteredUserRoles;
    }
}
