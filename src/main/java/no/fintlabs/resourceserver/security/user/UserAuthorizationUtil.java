package no.fintlabs.resourceserver.security.user;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserAuthorizationUtil {

    boolean userPermissionsConsumerEnabled;

    private static List<Long> convertSourceApplicationIdsStringToList(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String sourceApplicationIds = jwt.getClaimAsString("sourceApplicationIds");

        if (sourceApplicationIds.isEmpty() || sourceApplicationIds.isBlank()) {
            return List.of();
        }

        return Arrays.stream(sourceApplicationIds.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    public static void checkIfUserHasAccessToSourceApplication(
            boolean userPermissionsConsumerEnabled,
            Authentication authentication,
            Long sourceApplicationId
    ) {
        if (userPermissionsConsumerEnabled) {
            List<Long> allowedSourceApplicationIds =
                    convertSourceApplicationIdsStringToList(authentication);

            if (!allowedSourceApplicationIds.contains(sourceApplicationId)) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You do not have permission to access data for source application with id=" + sourceApplicationId
                );
            }
        }
    }

}
