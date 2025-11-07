package no.novari.flyt.resourceserver.security.integration.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.oauth2.jwt.Jwt;

@AllArgsConstructor
@Getter
public class TokenWrapper {
    private String tokenDescription;
    private Jwt token;

    public static TokenWrapper none() {
        return new TokenWrapper("None", null);
    }
}
