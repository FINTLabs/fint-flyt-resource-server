package no.fintlabs.resourceserver.security.client.sourceapplication;

import no.fintlabs.resourceserver.security.AuthorityMappingService;
import no.fintlabs.resourceserver.security.AuthorityPrefix;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class SourceApplicationAuthorizationService {

    private final AuthorityMappingService authorityMappingService;

    public SourceApplicationAuthorizationService(AuthorityMappingService authorityMappingService) {
        this.authorityMappingService = authorityMappingService;
    }

    // TODO 21/10/2025 eivindmorch: Fix. Should be part of lib API.
    public Long getSourceApplicationId(Authentication authentication) {
        Set<Long> sourceApplicationIds = authorityMappingService.extractLongValues(
                AuthorityPrefix.SOURCE_APPLICATION_ID,
                authentication.getAuthorities()
        );
        if (sourceApplicationIds.size() > 1) {
            throw new IllegalStateException("More than one source application id found");
        }
        return sourceApplicationIds.stream().findFirst().orElseThrow(
                () -> new IllegalStateException("No source application id found")
        );
    }

}
