package no.fintlabs.resourceserver.security.client.sourceapplication;

import lombok.AllArgsConstructor;
import no.fintlabs.resourceserver.security.AuthorityMappingService;
import no.fintlabs.resourceserver.security.AuthorityPrefix;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SourceApplicationAuthorityMappingService {

    private final AuthorityMappingService authorityMappingService;

    public Set<GrantedAuthority> createSourceApplicationAuthorities(Collection<Long> sourceApplicationIds) {
        return sourceApplicationIds.stream()
                .map(this::createSourceApplicationAuthority)
                .collect(Collectors.toSet());
    }

    public GrantedAuthority createSourceApplicationAuthority(Long sourceApplicationId) {
        return new SimpleGrantedAuthority(createSourceApplicationAuthorityString(sourceApplicationId));
    }

    public Set<String> createSourceApplicationAuthorityStrings(Collection<Long> sourceApplicationIds) {
        return sourceApplicationIds.stream()
                .map(this::createSourceApplicationAuthorityString)
                .collect(Collectors.toSet());
    }

    public String createSourceApplicationAuthorityString(Long sourceApplicationId) {
        return authorityMappingService.toAuthority(
                AuthorityPrefix.SOURCE_APPLICATION_ID,
                String.valueOf(sourceApplicationId)
        );
    }

}
