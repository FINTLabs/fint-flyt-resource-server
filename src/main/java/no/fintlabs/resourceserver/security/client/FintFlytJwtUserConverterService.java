package no.fintlabs.resourceserver.security.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.cache.FintCache;
import no.fintlabs.resourceserver.security.userpermission.UserPermission;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FintFlytJwtUserConverterService {

    private final FintCache<String, UserPermission> userPermissionCache;

    public Object modifyClaim(Object claim) {
        if (claim instanceof String) {
            return ((String) claim).replace("\\", "").replace("\"", "");
        }
        return claim;
    }

    public List<Long> convertSourceApplicationIdsStringToList(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String sourceApplicationIds = jwt.getClaimAsString("sourceApplicationIds");

        if (sourceApplicationIds.isEmpty() || sourceApplicationIds.isBlank()) {
            return List.of();
        }

        return Arrays.stream(sourceApplicationIds.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    public String convertSourceApplicationIdsIntoString(String objectIdentifier) {
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
