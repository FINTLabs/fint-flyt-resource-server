package no.fintlabs.resourceserver.security.client;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public class ClientAuthorizationUtil {

    public static String SOURCE_APPLICATION_ID_PREFIX = "SOURCE_APPLICATION_ID_";

    public static String getSourceApplicationId(Authentication authentication) {
        return authentication
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith(SOURCE_APPLICATION_ID_PREFIX))
                .findFirst()
                .map(authority -> authority.substring(SOURCE_APPLICATION_ID_PREFIX.length()))
                .orElseThrow();
    }
}
