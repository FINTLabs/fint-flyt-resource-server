package no.novari.flyt.resourceserver.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthorityMappingService {

    public static final String AUTHORITY_DELIMITER = "_";

    public String toAuthority(AuthorityPrefix prefix, String value) {
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

}
