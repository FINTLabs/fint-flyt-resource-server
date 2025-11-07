package no.novari.flyt.resourceserver.security.client.sourceapplication;

import no.novari.flyt.resourceserver.security.AuthorityMappingService;
import no.novari.flyt.resourceserver.security.AuthorityPrefix;
import no.novari.flyt.resourceserver.security.client.sourceapplication.exceptions.MultipleSourceApplicationIdsException;
import no.novari.flyt.resourceserver.security.client.sourceapplication.exceptions.NoSourceApplicationIdException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class SourceApplicationAuthorizationService {

    private final AuthorityMappingService authorityMappingService;

    public SourceApplicationAuthorizationService(AuthorityMappingService authorityMappingService) {
        this.authorityMappingService = authorityMappingService;
    }

    public Long getSourceApplicationId(Authentication authentication) {
        Set<Long> sourceApplicationIds = authorityMappingService.extractLongValues(
                AuthorityPrefix.SOURCE_APPLICATION_ID,
                authentication.getAuthorities()
        );
        if (sourceApplicationIds.size() > 1) {
            throw new MultipleSourceApplicationIdsException(sourceApplicationIds);
        }
        return sourceApplicationIds.stream().findFirst().orElseThrow(
                NoSourceApplicationIdException::new
        );
    }

}
