package no.fintlabs.resourceserver.security;

import no.fintlabs.resourceserver.security.client.InternalClientJwtConverter;
import no.fintlabs.resourceserver.security.client.sourceapplication.SourceApplicationAuthorization;
import no.fintlabs.resourceserver.security.client.sourceapplication.SourceApplicationAuthorizationRequestService;
import no.fintlabs.resourceserver.security.client.sourceapplication.SourceApplicationJwtConverter;
import no.fintlabs.resourceserver.security.user.UserClaim;
import no.fintlabs.resourceserver.security.user.UserJwtConverter;
import no.fintlabs.resourceserver.security.user.UserRole;
import org.junit.jupiter.api.Test;
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
class SecurityConfigurationIntegrationTest {

    @MockitoBean
    ReactiveJwtDecoder reactiveJwtDecoder;

    @MockitoBean
    SourceApplicationAuthorizationRequestService sourceApplicationAuthorizationRequestService;

    @Autowired
    private WebTestClient webTestClient;

    private static final String jwtString = "jwtString";

    @Test
    void shouldPermitAllAccessToActuatorEndpoints() {
        webTestClient.get()
                .uri("/actuator/dummy")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void shouldDenyAccessToInternalApiWithoutAuthentication() {
        webTestClient.get()
                .uri("/api/intern/dummy")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @ParameterizedTest
    @MethodSource("userAccessScenarios")
    void shouldAllowAccessToInternalApiForRoles(UserRole role, boolean isAllowed) {
        tokenContainsOrgIdAndRoles(
                UUID.fromString("753b9bb2-de61-41e7-995d-615e393c8f2a"),
                "domain-with-user-access.no",
                List.of(role.getClaimValue())
        );
        webTestClient.get()
                .uri("/api/intern/dummy")
                .headers(http -> http.setBearerAuth(jwtString))
                .exchange()
                .expectStatus().value(status -> {
                    if (isAllowed) {
                        assertThat(status).isEqualTo(HttpStatus.OK.value());
                    } else {
                        assertThat(status).isEqualTo(HttpStatus.FORBIDDEN.value());
                    }
                });
    }

    @Test
    void shouldForbidAccessToInternalApiWhenUnauthenticated() {
        webTestClient.get()
                .uri("/api/intern/dummy")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @ParameterizedTest
    @MethodSource("adminAccessScenarios")
    void shouldAllowOrDenyAccessToInternalAdminApiForRoles(UserRole role, boolean isAllowed) {
        tokenContainsOrgIdAndRoles(
                UUID.fromString("753b9bb2-de61-41e7-995d-615e393c8f2a"),
                "domain-with-user-access.no",
                List.of(role.getClaimValue())
        );
        webTestClient.get()
                .uri("/api/internal/admin/dummy")
                .headers(http -> http.setBearerAuth(jwtString))
                .exchange()
                .expectStatus().value(status -> {
                    if (isAllowed) {
                        assertThat(status).isEqualTo(HttpStatus.OK.value());
                    } else {
                        assertThat(status).isEqualTo(HttpStatus.FORBIDDEN.value());
                    }
                });
    }

    @Test
    void shouldAllowAccessToInternalClientApiWithValidAuthentication() {
        tokenContainsClientId("9e8118f3-9bc0-4f00-8675-c04bf8fe2494");

        webTestClient.get()
                .uri("/api/intern-klient/dummy")
                .headers(http -> http.setBearerAuth(jwtString))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void shouldForbidAccessToInternalClientApiWithUnknownClientId() {
        tokenContainsClientId("this-client-id-is-unauthorized");

        webTestClient.get()
                .uri("/api/intern-klient/dummy")
                .headers(http -> http.setBearerAuth(jwtString))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldNotAllowAccessToInternalClientApiWithoutAuthentication() {
        webTestClient.get()
                .uri("/api/intern-klient/dummy")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldAllowAccessToExternalApiWithAuthentication() {
        tokenContainsClientId("7ce63898-7485-4d37-bbd5-7bbefdf74c54");

        when(sourceApplicationAuthorizationRequestService.getClientAuthorization("7ce63898-7485-4d37-bbd5-7bbefdf74c54"))
                .thenReturn(Optional.of(SourceApplicationAuthorization
                        .builder()
                        .authorized(true)
                        .sourceApplicationId(1L)
                        .clientId("7ce63898-7485-4d37-bbd5-7bbefdf74c54")
                        .build())
                );

        webTestClient.get()
                .uri("/api/dummy")
                .headers(http -> http.setBearerAuth(jwtString))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void shouldForbidAccessToExternalApiWithAUnknownClientId() {
        tokenContainsClientId("unknown-client-id");

        webTestClient.get()
                .uri("/api/dummy")
                .headers(http -> http.setBearerAuth(jwtString))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldNotAllowAccessToExternalApiWithoutAuthentication() {
        webTestClient.get()
                .uri("/api/dummy")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void givenNoTokenAndAccessingCatchAllEndpointShouldReturnUnauthorized() {
        webTestClient.get()
                .uri("/some/unknown/path")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void givenTokenAndAccessingCatchAllEndpointShouldReturnForbidden() {
        tokenContainsOrgIdAndRoles(
                UUID.fromString("753b9bb2-de61-41e7-995d-615e393c8f2a"),
                "domain-with-user-access.no",
                List.of(UserRole.ADMIN.getClaimValue())
        );
        webTestClient.get()
                .uri("some/unknown/path")
                .headers(http -> http.setBearerAuth(jwtString))
                .exchange()
                .expectStatus().isUnauthorized();

    }

    private void tokenContainsOrgIdAndRoles(
            UUID objectIdentifier,
            String orgId,
            List<String> roles
    ) {
        when(reactiveJwtDecoder.decode(jwtString)).thenReturn(Mono.just(new Jwt(
                jwtString,
                Instant.now(),
                Instant.now().plusMillis(20000),
                Map.of("header1", "header1"),
                Map.of(
                        UserClaim.OBJECT_IDENTIFIER.getJwtTokenClaimName(), objectIdentifier,
                        UserClaim.ORGANIZATION_ID.getJwtTokenClaimName(), orgId,
                        UserClaim.ROLES.getJwtTokenClaimName(), roles
                )
        )));
    }

    private void tokenContainsClientId(
            String clientId
    ) {
        when(reactiveJwtDecoder.decode(jwtString)).thenReturn(Mono.just(new Jwt(
                jwtString,
                Instant.now(),
                Instant.now().plusMillis(20000),
                Map.of("header1", "header1"),
                Map.of(
                        "sub", clientId
                )
        )));
    }

    private static Stream<Object[]> userAccessScenarios() {
        return Stream.of(
                new Object[]{UserRole.USER, true},
                new Object[]{UserRole.ADMIN, true},
                new Object[]{UserRole.DEVELOPER, true}
        );
    }

    private static Stream<Object[]> adminAccessScenarios() {
        return Stream.of(
                new Object[]{UserRole.USER, false},
                new Object[]{UserRole.ADMIN, true},
                new Object[]{UserRole.DEVELOPER, true}
        );
    }
}