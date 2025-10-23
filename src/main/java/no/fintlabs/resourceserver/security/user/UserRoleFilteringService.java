package no.fintlabs.resourceserver.security.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.resourceserver.security.properties.InternalApiSecurityProperties;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRoleFilteringService {

    private final InternalApiSecurityProperties internalApiSecurityProperties;

    public Set<UserRole> filter(List<String> rolesStringList, String organizationId) {
        Set<UserRole> roleFilter = internalApiSecurityProperties.getUserRoleFilterPerOrgId()
                .getOrDefault(organizationId, Collections.emptySet());

        Set<UserRole> filteredUserRoles = rolesStringList
                .stream()
                .map(UserRole::getUserRoleFromValue)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        filteredUserRoles.retainAll(roleFilter);
        return filteredUserRoles;
    }
}
