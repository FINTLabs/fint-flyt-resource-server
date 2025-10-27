package no.fintlabs.resourceserver.security;

import no.fintlabs.resourceserver.security.client.InternalClientJwtConverter;
import no.fintlabs.resourceserver.security.client.sourceapplication.SourceApplicationAuthorizationRequestService;
import no.fintlabs.resourceserver.security.client.sourceapplication.SourceApplicationJwtConverter;
import no.fintlabs.resourceserver.security.user.UserClaim;
import no.fintlabs.resourceserver.security.user.UserJwtConverter;
import no.fintlabs.resourceserver.security.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    // --- Actuator chain ---
    @Test
    void shouldPermitAllAccessToActuatorEndpoints() {
        webTestClient.get()
                .uri("/actuator/dummy")
                .exchange()
                .expectStatus().isOk();
    }

    // --- Internal API requires authentication ---
    @Test
    void shouldDenyAccessToInternalApiWithoutAuthentication() {
        webTestClient.get()
                .uri("/api/intern/dummy")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldAllowAccessToInternalApiForUserRole() {
        tokenContainsOrgIdAndRoles(
                UUID.fromString("753b9bb2-de61-41e7-995d-615e393c8f2a"),
                "domain-with-user-access.no",
                List.of(UserRole.USER.getRoleValue())
        );
        webTestClient.get()
                .uri("/api/intern/dummy")
                .headers(http -> http.setBearerAuth(jwtString))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void shouldAllowAccessToInternalApiForAdminRole() {
        tokenContainsOrgIdAndRoles(
                UUID.fromString("753b9bb2-de61-41e7-995d-615e393c8f2a"),
                "domain-with-user-access.no",
                List.of(UserRole.ADMIN.getRoleValue())
        );
        webTestClient.get()
                .uri("/api/intern/dummy")
                .headers(http -> http.setBearerAuth(jwtString))
                .exchange()
                .expectStatus().isOk();
    }

//    @Test
//    void shouldAllowAccessToInternalApiForDevRole() {
//        tokenContainsOrgIdAndRoles(
//                UUID.fromString("753b9bb2-de61-41e7-995d-615e393c8f2a"),
//                "domain-with-user-access.no",
//                List.of(UserRole.ADMIN.getRoleValue())
//        );
//        WebTestClient.ResponseSpec ok = webTestClient.get()
//                .uri("/api/intern/dummy")
//                .headers(http -> http.setBearerAuth(jwtString))
//                .exchange()
//                .expectStatus().isOk();
//    }

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

    private void tokenDoesNotContainOrgId() {
        when(reactiveJwtDecoder.decode(jwtString)).thenReturn(Mono.just(new Jwt(
                jwtString,
                Instant.now(),
                Instant.now().plusMillis(20000),
                Map.of("header1", "header1"),
                Map.of("claim1", "claim1")
        )));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldDenyAccessToInternalAdminApiForNonAdmin() {
        webTestClient.get()
                .uri("/api/internal/admin/dummy")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAccessToInternalAdminApiForAdmin() {
        webTestClient.get()
                .uri("/api/internal/admin/dummy")
                .exchange()
                .expectStatus().isOk();
    }

    // --- Global chain ---
    @Test
    void shouldDenyAllOtherEndpointsByDefault() {
        webTestClient.get()
                .uri("/some/unknown/path")
                .exchange()
                .expectStatus().isForbidden();
    }
}