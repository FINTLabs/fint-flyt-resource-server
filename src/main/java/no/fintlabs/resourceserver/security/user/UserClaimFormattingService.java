package no.fintlabs.resourceserver.security.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.cache.FintCache;
import no.fintlabs.resourceserver.security.user.userpermission.UserPermission;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserClaimFormattingService {

    private final FintCache<String, UserPermission> userPermissionCache;

    Object removeDoubleQuotesFromClaim(String claim) {
        return claim.replace("\\", "").replace("\"", "");
    }

    String convertSourceApplicationIdsIntoString(String objectIdentifier) {
        String sourceApplicationIdsString = "";
        if (objectIdentifier != null) {
            Optional<UserPermission> userPermissionOptional = userPermissionCache.getOptional(objectIdentifier);

            if (userPermissionOptional.isPresent()) {
                List<Long> sourceApplicationIds = userPermissionOptional.get().getSourceApplicationIds();
                sourceApplicationIdsString = sourceApplicationIds.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","));
                log.debug("Fetched sourceApplicationIds from cache: {}", sourceApplicationIdsString);
            }
        }
        return sourceApplicationIdsString;
    }
}
