package no.fintlabs.resourceserver.security;

import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@AllArgsConstructor
public class SourceApplicationAuthorityMappingService {

    private final AuthorityMappingService authorityMappingService;

    public List<GrantedAuthority> createSourceApplicationAuthorities(Collection<Long> sourceApplicationIds) {
        return sourceApplicationIds.stream().map(this::createSourceApplicationAuthority).toList();
    }

    public GrantedAuthority createSourceApplicationAuthority(Long sourceApplicationId) {
        return new SimpleGrantedAuthority(createSourceApplicationAuthorityString(sourceApplicationId));
    }

    public String createSourceApplicationAuthorityString(Long sourceApplicationId) {
        return authorityMappingService.toAuthority(
                AuthorityPrefix.SOURCE_APPLICATION_ID,
                String.valueOf(sourceApplicationId)
        );
    }

}
