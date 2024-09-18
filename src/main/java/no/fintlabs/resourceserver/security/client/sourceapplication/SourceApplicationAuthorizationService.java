package no.fintlabs.resourceserver.security.client.sourceapplication;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class SourceApplicationAuthorizationService {

    public static String SOURCE_APPLICATION_ID_PREFIX = "SOURCE_APPLICATION_ID_";

    public GrantedAuthority getAuthority(SourceApplicationAuthorization sourceApplicationAuthorization) {
        return new SimpleGrantedAuthority(
                SOURCE_APPLICATION_ID_PREFIX + sourceApplicationAuthorization.getSourceApplicationId()
        );
    }

    public Long getSourceApplicationId(Authentication authentication) {
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
