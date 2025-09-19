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

import static no.fintlabs.resourceserver.UrlPaths.EXTERNAL_API;
import static no.fintlabs.resourceserver.UrlPaths.INTERNAL_API;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@WebFluxTest(controllers = {ExternalApiTestController.class, InternalApiTestController.class})
@Import({SecurityConfiguration.class, ClientJwtConverter.class, SourceApplicationJwtConverter.class, UserClaimFormattingService.class, TestMocksConfiguration.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles({"external-api", "internal-api"})
class InternalAndExternalApiEnabledTest {

    @Autowired
    ApplicationContext applicationContext;

    WebTestClient webTestClient;

    @Autowired
    private ReactiveJwtDecoder reactiveJwtDecoder;

    @Autowired
    private SourceApplicationAuthorizationRequestService clientAuthorizationRequestService;

    private final String externalApiUrl = EXTERNAL_API + "/dummy";
    private final String internalApiUrl = INTERNAL_API + "/dummy";
    private final String jwtString = "jwtString";

    @BeforeEach
    void setup() {
        webTestClient = WebTestClient
                .bindToApplicationContext(applicationContext)
                .apply(springSecurity())
                .build();
    }

    private void tokenContainsClientId(String clientId) {
        when(reactiveJwtDecoder.decode(jwtString)).thenReturn(Mono.just(new Jwt(
                jwtString,
                Instant.now(),
                Instant.now().plusMillis(20000),
                Map.of("header1", "header1"),
                Map.of("sub", clientId)
        )));
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

    @Test
    void given_token_with_clientId_that_is_authorized_the_request_should_return_ok() {
        tokenContainsClientId("clientId1234");
        webTestClient
                .mutateWith(mockJwt()
                        .jwt(jwt -> jwt.claim("sub", "clientId1234"))
                        .authorities(new SimpleGrantedAuthority("SOURCE_APPLICATION_ID_1")))
                .get()
                .uri(externalApiUrl)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void given_token_with_orgId_and_role_that_is_authorized_the_request_should_return_ok() {
        tokenContainsOrgIdAndRoles("example.no", List.of("admin"));
        webTestClient
                .mutateWith(mockJwt()
                        .jwt(jwt -> {
                            jwt.claim("organizationid", "example.no");
                            jwt.claim("organizationnumber", "organizationNumber");
                            jwt.claim("roles", List.of("admin"));
                        })
                        .authorities(new SimpleGrantedAuthority("ORGID_example.no_ROLE_admin")))
                .get()
                .uri(internalApiUrl)
                .exchange()
                .expectStatus().isOk();
    }
}
