package no.novari.flyt.resourceserver.security.user;

import no.novari.flyt.resourceserver.security.AuthorityMappingService;
import no.novari.flyt.resourceserver.security.AuthorityPrefix;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@Service
public class UserAuthorizationService {

    private final AuthorityMappingService authorityMappingService;

    public UserAuthorizationService(AuthorityMappingService authorityMappingService) {
        this.authorityMappingService = authorityMappingService;
    }

    public Set<Long> getUserAuthorizedSourceApplicationIds(Authentication authentication) {
        return authorityMappingService.extractLongValues(
                AuthorityPrefix.SOURCE_APPLICATION_ID,
                authentication.getAuthorities()
        );
    }

    public void checkIfUserHasAccessToSourceApplication(
            Authentication authentication,
            Long sourceApplicationId
    ) {
        if (!getUserAuthorizedSourceApplicationIds(authentication).contains(sourceApplicationId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not have permission to access or modify data that is related to source application with id="
                    + sourceApplicationId
            );
        }
    }

    public boolean userHasRole(Authentication authentication, UserRole role) {
        return authorityMappingService.extractStringValues(
                AuthorityPrefix.ROLE,
                authentication.getAuthorities()
        ).contains(role.name());

    }

}
