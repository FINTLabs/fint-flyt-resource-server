package no.fintlabs.resourceserver.security.client;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class ClientAuthorizationUtil {

    public static String CLIENT_ID_PREFIX = "CLIENT_ID_";

    public static GrantedAuthority getAuthority(String sub) {
        return new SimpleGrantedAuthority(CLIENT_ID_PREFIX + sub);
    }

}
