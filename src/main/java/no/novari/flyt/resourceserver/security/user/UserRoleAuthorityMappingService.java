package no.novari.flyt.resourceserver.security.user;

import lombok.AllArgsConstructor;
import no.novari.flyt.resourceserver.security.AuthorityMappingService;
import no.novari.flyt.resourceserver.security.AuthorityPrefix;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserRoleAuthorityMappingService {

    private final AuthorityMappingService authorityMappingService;

    public Set<GrantedAuthority> createRoleAuthorities(Collection<UserRole> roles) {
        return roles.stream()
                .map(this::createRoleAuthority)
                .collect(Collectors.toSet());
    }

    public GrantedAuthority createRoleAuthority(UserRole role) {
        return new SimpleGrantedAuthority(createRoleAuthorityString(role));
    }

    public Set<String> createRoleAuthorityStrings(Collection<UserRole> roles) {
        return roles.stream()
                .map(this::createRoleAuthorityString)
                .collect(Collectors.toSet());
    }

    public String createRoleAuthorityString(UserRole role) {
        return authorityMappingService.toAuthority(
                AuthorityPrefix.ROLE,
                role.name()
        );
    }

}
