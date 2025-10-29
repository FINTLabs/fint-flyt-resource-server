package no.fintlabs.resourceserver.security.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

@Getter
@AllArgsConstructor
public enum UserRole {
    USER("https://role-catalog.vigoiks.no/vigo/flyt/user"),
    DEVELOPER("https://role-catalog.vigoiks.no/vigo/flyt/developer"),
    ADMIN("https://role-catalog.vigoiks.no/vigo/flyt/admin");

    private final String claimValue;

    private static final Map<String, UserRole> userRoleByClaimValue =
            Arrays.stream(UserRole.values())
                    .collect(toMap(
                            UserRole::getClaimValue,
                            Function.identity()
                    ));

    public static Optional<UserRole> getUserRoleFromValue(String roleValue) {
        return Optional.ofNullable(userRoleByClaimValue.getOrDefault(roleValue, null));
    }

}
