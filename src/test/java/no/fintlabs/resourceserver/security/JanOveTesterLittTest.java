package no.fintlabs.resourceserver.security;

import no.fintlabs.resourceserver.security.client.InternalClientJwtConverter;
import no.fintlabs.resourceserver.security.client.sourceapplication.SourceApplicationAuthorization;
import no.fintlabs.resourceserver.security.client.sourceapplication.SourceApplicationAuthorizationRequestService;
import no.fintlabs.resourceserver.security.client.sourceapplication.SourceApplicationJwtConverter;
import no.fintlabs.resourceserver.security.user.UserClaim;
import no.fintlabs.resourceserver.security.user.UserJwtConverter;
import no.fintlabs.resourceserver.security.user.UserRole;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("all-api-enabled")
@Import({SecurityConfiguration.class, InternalClientJwtConverter.class, SourceApplicationJwtConverter.class, UserJwtConverter.class})
class JanOveTesterLittTest {

    @MockitoBean
    ReactiveJwtDecoder reactiveJwtDecoder;

    @MockitoBean
    SourceApplicationAuthorizationRequestService sourceApplicationAuthorizationRequestService;

    @Autowired
    WebTestClient webTestClient;

    private static final String jwtString = "jwtString";

    // --- Parameterized scenario model ---

    private record SecurityScenario(
            String description,
            String path,
            String method,
            TokenType tokenType,
            List<UserRole> roles,
            HttpStatus expectedStatus,
            boolean mockAuthorizationService,
            boolean validClient
    ) {
    }

    private enum TokenType {
        NONE,
        USER,
        USER_MISSING_ORG_ID,
        USER_MISSING_OBJECT_ID,
        CLIENT
    }

    // --- Parameterized test ---

    @ParameterizedTest(name = "{index} => {0}")
    @MethodSource("securityScenarios")
    void shouldEnforceSecurityRules(SecurityScenario scenario) {
        // Arrange
        switch (scenario.tokenType()) {
            case USER -> mockUserToken(scenario.roles());
            case USER_MISSING_ORG_ID -> mockUserTokenWithoutOrgId(scenario.roles());
            case USER_MISSING_OBJECT_ID -> mockUserTokenWithoutObjectId(scenario.roles());
            case CLIENT -> mockClientToken(scenario.validClient(), scenario.mockAuthorizationService());
            case NONE -> {
            } // no setup
        }

        var request = webTestClient.get().uri(scenario.path());
        if (scenario.tokenType() != TokenType.NONE) {
            request.headers(h -> h.setBearerAuth(jwtString));
        }

        // Act + Assert
        request.exchange()
                .expectStatus()
                .value(status -> assertThat(status).isEqualTo(scenario.expectedStatus().value()));
    }

    // --- Scenario generation ---

    static Stream<SecurityScenario> securityScenarios() {
        return Stream.of(
               //  INTERNAL ADMIN API
                new SecurityScenario("Admin API - USER should be forbidden",
                        "/api/internal/admin/dummy", "GET", TokenType.USER,
                        List.of(UserRole.USER), HttpStatus.FORBIDDEN, false, false),
                new SecurityScenario("Admin API - ADMIN should be allowed",
                        "/api/internal/admin/dummy", "GET", TokenType.USER,
                        List.of(UserRole.ADMIN), HttpStatus.OK, false, false)
                //,
//                new SecurityScenario("Admin API - DEVELOPER should be allowed",
//                        "/api/internal/admin/dummy", "GET", TokenType.USER,
//                        List.of(UserRole.DEVELOPER), HttpStatus.OK, false, false),
//                new SecurityScenario("Admin API - no token -> unauthorized",
//                        "/api/internal/admin/dummy", "GET", TokenType.NONE,
//                        null, HttpStatus.UNAUTHORIZED, false, false),
//                new SecurityScenario("Admin API - missing orgId -> error (unauthorized)",
//                        "/api/internal/admin/dummy", "GET", TokenType.USER_MISSING_ORG_ID,
//                        List.of(UserRole.ADMIN), HttpStatus.UNAUTHORIZED, false, false),
//                new SecurityScenario("Admin API - missing objectId -> error (unauthorized)",
//                        "/api/internal/admin/dummy", "GET", TokenType.USER_MISSING_OBJECT_ID,
//                        List.of(UserRole.ADMIN), HttpStatus.UNAUTHORIZED, false, false),
//
//                // INTERNAL API
//                new SecurityScenario("Internal API - USER allowed",
//                        "/api/intern/dummy", "GET", TokenType.USER,
//                        List.of(UserRole.USER), HttpStatus.OK, false, false),
//                new SecurityScenario("Internal API - ADMIN allowed",
//                        "/api/intern/dummy", "GET", TokenType.USER,
//                        List.of(UserRole.ADMIN), HttpStatus.OK, false, false),
//                new SecurityScenario("Internal API - no token -> unauthorized",
//                        "/api/intern/dummy", "GET", TokenType.NONE,
//                        null, HttpStatus.UNAUTHORIZED, false, false),
//
//                // INTERNAL CLIENT API
//                new SecurityScenario("Internal Client API - valid client -> ok",
//                        "/api/intern-klient/dummy", "GET", TokenType.CLIENT,
//                        null, HttpStatus.OK, false, true),
//                new SecurityScenario("Internal Client API - invalid client -> forbidden",
//                        "/api/intern-klient/dummy", "GET", TokenType.CLIENT,
//                        null, HttpStatus.FORBIDDEN, false, false),
//                new SecurityScenario("Internal Client API - no token -> unauthorized",
//                        "/api/intern-klient/dummy", "GET", TokenType.NONE,
//                        null, HttpStatus.UNAUTHORIZED, false, false),
//
//                // EXTERNAL API
//                new SecurityScenario("External API - valid client -> ok",
//                        "/api/dummy", "GET", TokenType.CLIENT,
//                        null, HttpStatus.OK, true, true),
//                new SecurityScenario("External API - invalid client -> forbidden",
//                        "/api/dummy", "GET", TokenType.CLIENT,
//                        null, HttpStatus.FORBIDDEN, false, false),
//                new SecurityScenario("External API - no token -> unauthorized",
//                        "/api/dummy", "GET", TokenType.NONE,
//                        null, HttpStatus.UNAUTHORIZED, false, false),
//
//                // CATCH-ALL
//                new SecurityScenario("Catch-all - no token -> unauthorized",
//                        "/some/unknown/path", "GET", TokenType.NONE,
//                        null, HttpStatus.UNAUTHORIZED, false, false),
//                new SecurityScenario("Catch-all - token but no match -> unauthorized",
//                        "/some/unknown/path", "GET", TokenType.USER,
//                        List.of(UserRole.ADMIN), HttpStatus.UNAUTHORIZED, false, false)
        );
    }

    // --- Mock setup helpers ---

    private void mockUserToken(List<UserRole> roles) {
        when(reactiveJwtDecoder.decode(jwtString)).thenReturn(Mono.just(new Jwt(
                jwtString,
                Instant.now(),
                Instant.now().plusSeconds(60),
                Map.of("header", "value"),
                Map.of(
                        UserClaim.OBJECT_IDENTIFIER.getJwtTokenClaimName(), UUID.randomUUID().toString(),
                        UserClaim.ORGANIZATION_ID.getJwtTokenClaimName(), "domain.no",
                        UserClaim.ROLES.getJwtTokenClaimName(), roles.stream().map(UserRole::getClaimValue).toList()
                )
        )));
    }

    private void mockUserTokenWithoutOrgId(List<UserRole> roles) {
        when(reactiveJwtDecoder.decode(jwtString)).thenReturn(Mono.just(new Jwt(
                jwtString,
                Instant.now(),
                Instant.now().plusSeconds(60),
                Map.of(),
                Map.of(
                        UserClaim.OBJECT_IDENTIFIER.getJwtTokenClaimName(), UUID.randomUUID().toString(),
                        UserClaim.ROLES.getJwtTokenClaimName(), roles.stream().map(UserRole::getClaimValue).toList()
                )
        )));
    }

    private void mockUserTokenWithoutObjectId(List<UserRole> roles) {
        when(reactiveJwtDecoder.decode(jwtString)).thenReturn(Mono.just(new Jwt(
                jwtString,
                Instant.now(),
                Instant.now().plusSeconds(60),
                Map.of(),
                Map.of(
                        UserClaim.ORGANIZATION_ID.getJwtTokenClaimName(), "domain.no",
                        UserClaim.ROLES.getJwtTokenClaimName(), roles.stream().map(UserRole::getClaimValue).toList()
                )
        )));
    }

    private void mockClientToken(boolean validClient, boolean mockAuthorizationService) {
        String clientId = validClient
                ? "7ce63898-7485-4d37-bbd5-7bbefdf74c54"
                : "unauthorized-client-id";

        when(reactiveJwtDecoder.decode(jwtString)).thenReturn(Mono.just(new Jwt(
                jwtString,
                Instant.now(),
                Instant.now().plusSeconds(60),
                Map.of(),
                Map.of("sub", clientId)
        )));

        if (mockAuthorizationService) {
            when(sourceApplicationAuthorizationRequestService.getClientAuthorization(clientId))
                    .thenReturn(Optional.of(SourceApplicationAuthorization.builder()
                            .authorized(validClient)
                            .clientId(clientId)
                            .sourceApplicationId(validClient ? 1L : null)
                            .build()));
        }
    }
}