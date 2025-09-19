package no.fintlabs.resourceserver.security;

import no.fintlabs.resourceserver.security.properties.InternalApiSecurityProperties;
import no.fintlabs.resourceserver.security.user.UserClaimFormattingService;
import no.fintlabs.resourceserver.security.user.UserJwtConverter;
import no.fintlabs.resourceserver.testutils.JwtFactory;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UserJwtConverterTest {

    private final InternalApiSecurityProperties properties = Mockito.mock(InternalApiSecurityProperties.class);
    private final UserClaimFormattingService userClaimFormattingService = Mockito.mock(UserClaimFormattingService.class);
    private final UserJwtConverter converter = new UserJwtConverter(properties, userClaimFormattingService);

    @Test
    void converting_fint_user_jwt_should_result_in_three_authorities() {
        AbstractAuthenticationToken authenticationToken = converter.convert(JwtFactory.createEndUserJwt()).block();
        assertNotNull(authenticationToken);
        assertEquals(3, authenticationToken.getAuthorities().size());
    }

    @Test
    void converting_user_jwt_should_remove_backslash_and_quote_from_claims() {
        Jwt jwt = JwtFactory.createEndUserJwt();
        Mono<AbstractAuthenticationToken> convertedToken = converter.convert(jwt);
        JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) convertedToken.block();
        assertNotNull(jwtAuthenticationToken);
        Jwt modifiedJwt = jwtAuthenticationToken.getToken();
        modifiedJwt.getClaims().forEach((key, value) -> {
            if (value instanceof String s) {
                assertFalse(s.contains("\\"));
                assertFalse(s.contains("\""));
            }
        });
    }
}
