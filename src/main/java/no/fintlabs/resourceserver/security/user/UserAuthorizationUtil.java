package no.fintlabs.resourceserver.security.user;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserAuthorizationUtil {

    public static List<Long> convertSourceApplicationIdsStringToList(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String sourceApplicationIds = jwt.getClaimAsString("sourceApplicationIds");

        if (sourceApplicationIds.isEmpty() || sourceApplicationIds.isBlank()) {
            return List.of();
        }

        return Arrays.stream(sourceApplicationIds.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

}
