package no.novari.flyt.resourceserver.security.client.internal;

import lombok.AllArgsConstructor;
import no.novari.flyt.resourceserver.security.AuthorityMappingService;
import no.novari.flyt.resourceserver.security.AuthorityPrefix;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
public class InternalClientAuthorityMappingService {

    private final AuthorityMappingService authorityMappingService;

    public GrantedAuthority createInternalClientIdAuthority(String clientId) {
        return new SimpleGrantedAuthority(createInternalClientIdAuthorityString(clientId));
    }

    public Set<String> createInternalClientIdAuthorityStrings(Collection<String> clientIds) {
        return clientIds.stream()
                .map(this::createInternalClientIdAuthorityString)
                .collect(Collectors.toSet());
    }

    public String createInternalClientIdAuthorityString(String clientId) {
        return authorityMappingService.toAuthority(
                AuthorityPrefix.CLIENT_ID,
                clientId
        );
    }

}
