package no.fintlabs.resourceserver.security;

import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ClientAuthorityMappingService {

    private final AuthorityMappingService authorityMappingService;

    public GrantedAuthority createInternalClientIdAuthority(String clientId) {
        return new SimpleGrantedAuthority(createInternalClientIdAuthorityString(clientId));
    }

    public String createInternalClientIdAuthorityString(String clientId) {
        return authorityMappingService.toAuthority(
                AuthorityPrefix.CLIENT_ID,
                clientId
        );
    }

}
