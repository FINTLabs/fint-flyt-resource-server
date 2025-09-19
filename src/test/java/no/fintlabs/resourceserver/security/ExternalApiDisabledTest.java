package no.fintlabs.resourceserver.security;

import no.fintlabs.resourceserver.security.client.sourceapplication.SourceApplicationAuthorization;
import no.fintlabs.resourceserver.security.client.sourceapplication.SourceApplicationAuthorizationRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static no.fintlabs.resourceserver.UrlPaths.EXTERNAL_API;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

@WebFluxTest(controllers = ExternalApiTestController.class)
@Import({SecurityConfiguration.class, TestMocksConfiguration.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("internal-api")
class ExternalApiDisabledTest {

    @Autowired
    ApplicationContext applicationContext;

    WebTestClient webTestClient;

    @Autowired
    private ReactiveJwtDecoder reactiveJwtDecoder;

    @Autowired
    private SourceApplicationAuthorizationRequestService clientAuthorizationRequestService;

    private final String externalApiUrl = EXTERNAL_API + "/dummy";
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

    private void clientIsAuthorized(String clientId, String sourceApplicationId) {
        when(clientAuthorizationRequestService.getClientAuthorization(clientId)).thenReturn(Optional.of(
                SourceApplicationAuthorization
                        .builder()
                        .authorized(true)
                        .clientId(clientId)
                        .sourceApplicationId(sourceApplicationId)
                        .build()
        ));
    }

    @Test
    void given_token_with_clientId_that_is_authorized_the_request_should_return_unauthorized() {
        tokenContainsClientId("clientId1234");
        clientIsAuthorized("clientId1234", "1");
        webTestClient.get()
                .uri(externalApiUrl)
                .headers(http -> http.setBearerAuth(jwtString))
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
