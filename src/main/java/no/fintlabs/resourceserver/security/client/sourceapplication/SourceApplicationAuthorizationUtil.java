package no.fintlabs.resourceserver.security.client.sourceapplication;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class SourceApplicationAuthorizationUtil {

    public static String SOURCE_APPLICATION_ID_PREFIX = "SOURCE_APPLICATION_ID_";

    public static GrantedAuthority getAuthority(SourceApplicationAuthorization sourceApplicationAuthorization) {
        return new SimpleGrantedAuthority(
                SOURCE_APPLICATION_ID_PREFIX + sourceApplicationAuthorization.getSourceApplicationId()
        );
    }

    public static Long getSourceApplicationId(Authentication authentication) {
        return authentication
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith(SOURCE_APPLICATION_ID_PREFIX))
                .findFirst()
                .map(authority -> authority.substring(SOURCE_APPLICATION_ID_PREFIX.length()))
                .map(Long::parseLong)
                .orElseThrow();
    }

}
