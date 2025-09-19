package no.fintlabs.resourceserver.security;

import no.fintlabs.resourceserver.security.client.sourceapplication.SourceApplicationAuthorizationRequestService;
import no.fintlabs.resourceserver.security.client.ClientJwtConverter;
import no.fintlabs.resourceserver.security.client.sourceapplication.SourceApplicationJwtConverter;
import no.fintlabs.resourceserver.security.user.UserClaimFormattingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static no.fintlabs.resourceserver.UrlPaths.INTERNAL_API;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@WebFluxTest(controllers = InternalApiTestController.class)
@Import({SecurityConfiguration.class, ClientJwtConverter.class, SourceApplicationJwtConverter.class, UserClaimFormattingService.class, TestMocksConfiguration.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("internal-api")
class InternalApiEnabledTest {

    @Autowired
    ApplicationContext applicationContext;

    WebTestClient webTestClient;

    @Autowired
    private ReactiveJwtDecoder reactiveJwtDecoder;

    @Autowired
    private SourceApplicationAuthorizationRequestService clientAuthorizationRequestService;

    private final String internalApiUrl = INTERNAL_API + "/dummy";
    private final String jwtString = "jwtString";

    @BeforeEach
    void setup() {
        webTestClient = WebTestClient
                .bindToApplicationContext(applicationContext)
                .apply(springSecurity())
                .build();
    }

    private void tokenContainsOrgIdAndRoles(String orgId, List<String> roles) {
        when(reactiveJwtDecoder.decode(jwtString)).thenReturn(Mono.just(new Jwt(
                jwtString,
                Instant.now(),
                Instant.now().plusMillis(20000),
                Map.of("header1", "header1"),
                Map.of(
                        "organizationid", orgId,
                        "organizationnumber", "organizationNumber",
                        "roles", roles
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
    void given_no_token_should_return_unauthorized() {
        webTestClient.get().uri(internalApiUrl).exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void given_token_without_orgId_should_return_forbidden() {
        tokenDoesNotContainOrgId();
        webTestClient
                .mutateWith(mockJwt())
                .get().uri(internalApiUrl)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void given_token_with_orgId_and_role_that_is_authorized_the_request_should_return_ok() {
        tokenContainsOrgIdAndRoles("example.no", List.of("admin"));
        webTestClient
                .mutateWith(mockJwt()
                        .jwt(jwt -> {
                            jwt.claim("organizationid", "example.no");
                            jwt.claim("roles", List.of("admin"));
                        })
                        .authorities(new SimpleGrantedAuthority("ORGID_example.no_ROLE_admin")))
                .get().uri(internalApiUrl)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void given_token_with_orgId_and_multiple_roles_where_at_least_one_is_authorized_the_request_should_return_ok() {
        tokenContainsOrgIdAndRoles("example.no", List.of("admin", "user"));
        webTestClient
                .mutateWith(mockJwt()
                        .jwt(jwt -> {
                            jwt.claim("organizationid", "example.no");
                            jwt.claim("roles", List.of("admin", "user"));
                        })
                        .authorities(new SimpleGrantedAuthority("ORGID_example.no_ROLE_admin")))
                .get().uri(internalApiUrl)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void given_token_with_orgId_and_role_that_is_not_authorized_the_request_should_return_forbidden() {
        tokenContainsOrgIdAndRoles("example.no", List.of("user"));
        webTestClient
                .mutateWith(mockJwt()
                        .jwt(jwt -> {
                            jwt.claim("organizationid", "example.no");
                            jwt.claim("roles", List.of("user"));
                        })
                        .authorities(new SimpleGrantedAuthority("ORGID_example.no_ROLE_user")))
                .get().uri(internalApiUrl)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void given_token_without_roles_should_return_forbidden() {
        tokenContainsOrgIdAndRoles("example.no", List.of());
        webTestClient
                .mutateWith(mockJwt()
                        .jwt(jwt -> jwt.claim("organizationid", "example.no")))
                .get().uri(internalApiUrl)
                .exchange()
                .expectStatus().isForbidden();
    }
}
