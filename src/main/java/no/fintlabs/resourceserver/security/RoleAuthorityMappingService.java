package no.fintlabs.resourceserver.security;

import lombok.AllArgsConstructor;
import no.fintlabs.resourceserver.security.user.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@AllArgsConstructor
public class RoleAuthorityMappingService {

    private final AuthorityMappingService authorityMappingService;

    public List<GrantedAuthority> createRoleAuthorities(Collection<UserRole> roles) {
        return roles.stream()
                .map(this::createRoleAuthority)
                .toList();
    }

    public GrantedAuthority createRoleAuthority(UserRole role) {
        return new SimpleGrantedAuthority(createRoleAuthorityString(role));
    }

    public List<String> createRoleAuthorityStrings(Collection<UserRole> roles) {
        return roles.stream()
                .map(this::createRoleAuthorityString)
                .toList();
    }

    public String createRoleAuthorityString(UserRole role) {
        return authorityMappingService.toAuthority(
                AuthorityPrefix.ROLE,
                role.name()
        );
    }

}
