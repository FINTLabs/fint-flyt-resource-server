package no.fintlabs.resourceserver.security;

import no.fintlabs.resourceserver.security.client.sourceapplication.SourceApplicationAuthorization;
import no.fintlabs.resourceserver.security.client.sourceapplication.SourceApplicationAuthorizationRequestService;
import no.fintlabs.resourceserver.security.client.sourceapplication.SourceApplicationJwtConverter;
import no.fintlabs.resourceserver.security.client.ClientJwtConverter;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

@WebFluxTest(controllers = ExternalApiTestController.class)
@Import({SecurityConfiguration.class, ClientJwtConverter.class, SourceApplicationJwtConverter.class, UserClaimFormattingService.class, TestMocksConfiguration.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("external-api")
class ExternalApiEnabledTest {

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

    private void tokenDoesNotContainClientId() {
        when(reactiveJwtDecoder.decode(jwtString)).thenReturn(Mono.just(new Jwt(
                jwtString,
                Instant.now(),
                Instant.now().plusMillis(20000),
                Map.of("header1", "header1"),
                Map.of("claim1", "claim1")
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

    private void clientIsNotAuthorized(String clientId) {
        when(clientAuthorizationRequestService.getClientAuthorization(clientId)).thenReturn(Optional.of(
                SourceApplicationAuthorization
                        .builder()
                        .authorized(false)
                        .clientId(clientId)
                        .build()
        ));
    }

    private void authorizationRequestReturnsEmpty(String clientId) {
        when(clientAuthorizationRequestService.getClientAuthorization(clientId)).thenReturn(Optional.empty());
    }

    @Test
    void given_no_token_should_not_call_clientAuthorizationRequestService() {
        webTestClient.get().uri(externalApiUrl).exchange();
        verify(clientAuthorizationRequestService, never()).getClientAuthorization(any());
    }

    @Test
    void given_no_clientId_should_not_call_clientAuthorizationRequestService() {
        tokenDoesNotContainClientId();
        webTestClient.get().uri(externalApiUrl).headers(http -> http.setBearerAuth(jwtString)).exchange();
        verify(clientAuthorizationRequestService, never()).getClientAuthorization(any());
    }

    @Test
    void given_token_with_clientId_should_call_clientAuthorizationRequestService_with_clientId() {
        tokenContainsClientId("clientId1234");
        webTestClient.get().uri(externalApiUrl).headers(http -> http.setBearerAuth(jwtString)).exchange();
        verify(clientAuthorizationRequestService, times(1)).getClientAuthorization(any());
    }

    @Test
    void given_no_token_should_return_unauthorized() {
        webTestClient.get().uri(externalApiUrl).exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void given_token_without_clientId_should_return_forbidden() {
        tokenDoesNotContainClientId();
        webTestClient
                .mutateWith(org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt().jwt(jwt -> jwt.claim("claim1", "claim1")))
                .get()
                .uri(externalApiUrl)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void given_token_with_clientId_that_is_not_authorized_should_return_forbidden() {
        tokenContainsClientId("clientId1234");
        clientIsNotAuthorized("clientId1234");
        webTestClient.get().uri(externalApiUrl).headers(http -> http.setBearerAuth(jwtString)).exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void given_token_with_clientId_that_is_authorized_the_request_should_return_ok() {
        tokenContainsClientId("clientId1234");
        clientIsAuthorized("clientId1234", "1");
        webTestClient.get().uri(externalApiUrl).headers(http -> http.setBearerAuth(jwtString)).exchange()
                .expectStatus().isOk();
    }

    @Test
    void given_token_with_clientId_but_no_empty_response_from_internal_authorization_request_should_return_forbidden() {
        tokenContainsClientId("clientId1234");
        authorizationRequestReturnsEmpty("clientId1234");
        webTestClient
                .mutateWith(org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt().jwt(jwt -> jwt.claim("sub", "clientId1234")))
                .get()
                .uri(externalApiUrl)
                .exchange()
                .expectStatus().isForbidden();
    }
}
