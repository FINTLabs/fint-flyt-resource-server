package no.fintlabs.resourceserver.security;

import no.fintlabs.resourceserver.security.user.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthorityMappingService {

    public static String AUTHORITY_DELIMITER = "_";

    public String[] toAuthoritiesStringArray(AuthorityPrefix prefix, List<String> values) {
        return values
                .stream()
                .map(value -> toAuthorityString(prefix, value))
                .toArray(String[]::new);
    }

    public String toAuthorityString(AuthorityPrefix prefix, String value) {
        return prefix.getValue() + AUTHORITY_DELIMITER + value;
    }

    public Set<Long> extractLongValues(AuthorityPrefix prefix, Collection<? extends GrantedAuthority> authorities) {
        return extractStringValues(prefix, authorities)
                .stream()
                .map(Long::parseLong)
                .collect(Collectors.toSet());
    }

    public Set<String> extractStringValues(AuthorityPrefix prefix, Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith(prefix.getValue()))
                .map(authority -> authority.substring(prefix.getValue().length() + AUTHORITY_DELIMITER.length()))
                .collect(Collectors.toSet());
    }

    public List<GrantedAuthority> createRoleAuthorities(Collection<UserRole> roles) {
        return roles.stream().map(this::createRoleAuthority).toList();
    }

    public GrantedAuthority createRoleAuthority(UserRole role) {
        return new SimpleGrantedAuthority(
                toAuthorityString(
                        AuthorityPrefix.ROLE,
                        role.getRoleValue()
                )
        );
    }

    public List<GrantedAuthority> createSourceApplicationAuthorities(Collection<Long> sourceApplicationIds) {
        return sourceApplicationIds.stream().map(this::createSourceApplicationAuthority).toList();
    }

    public GrantedAuthority createSourceApplicationAuthority(Long sourceApplicationId) {
        return new SimpleGrantedAuthority(toAuthorityString(
                AuthorityPrefix.SOURCE_APPLICATION_ID,
                String.valueOf(sourceApplicationId)
        ));
    }

    public GrantedAuthority createInternalClientIdAuthority(String clientId) {
        return new SimpleGrantedAuthority(toAuthorityString(
                AuthorityPrefix.CLIENT_ID,
                clientId
        ));
    }

//    public List<GrantedAuthority> createOrgAndRoleAuthorities(String orgId, List<UserRole> userRoles) {
//        return userRoles
//                .stream()
//                .map(userRole -> createOrgAndRoleAuthority(orgId, userRole))
//                .toList();
//    }
//
//    public GrantedAuthority createOrgAndRoleAuthority(String orgId, UserRole userRole) {
//        return new SimpleGrantedAuthority(
//                new StringJoiner(AUTHORITY_DELIMITER)
//                        .add(AuthorityPrefix.ORG_ID.getValue())
//                        .add(orgId)
//                        .add(AuthorityPrefix.ROLE.getValue())
//                        .add(userRole.getRoleValue())
//                        .toString()
//        );
//    }

}
