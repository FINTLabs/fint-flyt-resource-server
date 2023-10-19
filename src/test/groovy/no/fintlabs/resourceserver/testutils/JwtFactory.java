package no.fintlabs.resourceserver.testutils;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Arrays;

public class JwtFactory {

    public static Jwt createEndUserJwt() {
        return Jwt.withTokenValue("test")
                .header("kid", "123456789")
                .header("typ", "JWT")
                .header("alg", "RS256")
                .claims(claims -> {
                    claims.put("surname", "Testesen");
                    claims.put("givenname", "Test");
                    claims.put("email", "test@test.com");
                    claims.put("studentnumber", "123456");
                    claims.put("employeeId", "654321");
                    claims.put("organizationid", "test.com");
                    claims.put("organizationnumber", "123456789");
                    claims.put("roles", Arrays.asList("role1", "role2", "role3"));
                })
                .build();
    }
}
