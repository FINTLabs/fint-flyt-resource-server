package no.fintlabs.resourceserver.security;

import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@AllArgsConstructor
public class ClientAuthorityMappingService {

    private final AuthorityMappingService authorityMappingService;

    public GrantedAuthority createInternalClientIdAuthority(String clientId) {
        return new SimpleGrantedAuthority(createInternalClientIdAuthorityString(clientId));
    }

    public List<String> createInternalClientIdAuthorityStrings(Collection<String> clientIds) {
        return clientIds.stream()
                .map(this::createInternalClientIdAuthorityString)
                .toList();
    }

    public String createInternalClientIdAuthorityString(String clientId) {
        return authorityMappingService.toAuthority(
                AuthorityPrefix.CLIENT_ID,
                clientId
        );
    }

}
