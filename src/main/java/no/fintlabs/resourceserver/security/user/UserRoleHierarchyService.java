package no.fintlabs.resourceserver.security.user;

import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserRoleHierarchyService {

    private static final Map<UserRole, Set<UserRole>> IMPLIED_ROLES_PER_ROLE = Map.of(
            UserRole.USER, Set.of(),
            UserRole.ADMIN, Set.of(UserRole.USER),
            UserRole.DEVELOPER, Set.of(UserRole.ADMIN)
    );

    public Set<UserRole> getProvidedAndImpliedRoles(Collection<UserRole> roles) {
        return roles.stream()
                .map(this::getProvidedAndImpliedRoles)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    private Set<UserRole> getProvidedAndImpliedRoles(UserRole role) {
        Set<UserRole> userRoles = IMPLIED_ROLES_PER_ROLE.get(role)
                .stream()
                .map(this::getProvidedAndImpliedRoles)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        userRoles.add(role);
        return userRoles;
    }

}
