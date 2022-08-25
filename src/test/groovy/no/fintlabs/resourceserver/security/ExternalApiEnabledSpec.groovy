package no.fintlabs.resourceserver.security

import no.fintlabs.resourceserver.security.client.ClientAuthorization
import no.fintlabs.resourceserver.security.client.ClientAuthorizationRequestService
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.ApplicationContext
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import spock.lang.Specification

import java.time.Instant

import static no.fintlabs.UrlPaths.EXTERNAL_API
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity

@WebFluxTest(controllers = ExternalApiTestController.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("external-api")
class ExternalApiEnabledSpec extends Specification {

    @Autowired
    ApplicationContext applicationContext

    WebTestClient webTestClient

    @SpringBean
    private ReactiveJwtDecoder reactiveJwtDecoder = Mock(ReactiveJwtDecoder.class)

    @SpringBean
    ClientAuthorizationRequestService clientAuthorizationRequestService = Mock(ClientAuthorizationRequestService.class)

    private final String externalApiUrl = EXTERNAL_API + "/dummy"

    private final String jwtString = "jwtString"

    def setup() {
        webTestClient = WebTestClient
                .bindToApplicationContext(applicationContext)
                .apply(springSecurity())
                .build()
    }

    private void tokenContainsClientId(String clientId) {
        reactiveJwtDecoder.decode(jwtString) >> Mono.just(new Jwt(
                jwtString,
                Instant.now(),
                Instant.now().plusMillis(20000),
                Map.of("header1", "header1"),
                Map.of("sub", clientId)
        ))
    }

    private void tokenDoesNotContainClientId() {
        reactiveJwtDecoder.decode(jwtString) >> Mono.just(new Jwt(
                jwtString,
                Instant.now(),
                Instant.now().plusMillis(20000),
                Map.of("header1", "header1"),
                Map.of("claim1", "claim1")
        ))
    }

    private void clientIsAuthorized(String clientId, String sourceApplicationId) {
        clientAuthorizationRequestService.getClientAuthorization(clientId) >> Optional.of(
                ClientAuthorization
                        .builder()
                        .authorized(true)
                        .clientId(clientId)
                        .sourceApplicationId(sourceApplicationId)
                        .build()
        )
    }

    private void clientIsNotAuthorized(String clientId) {
        clientAuthorizationRequestService.getClientAuthorization(clientId) >> Optional.of(
                ClientAuthorization
                        .builder()
                        .authorized(false)
                        .clientId(clientId)
                        .build()
        )
    }

    private void authorizationRequestReturnsEmpty(String clientId) {
        clientAuthorizationRequestService.getClientAuthorization(clientId) >> Optional.empty()
    }

    def 'given_no_token_should_not_call_clientAuthorizationRequestService'() {
        when:
        webTestClient
                .get()
                .uri(externalApiUrl)
                .exchange()

        then:
        0 * clientAuthorizationRequestService.getClientAuthorization(_) >> Optional.empty()
    }

    def 'given_no_clientId_should_not_call_clientAuthorizationRequestService'() {
        given:
        tokenDoesNotContainClientId()

        when:
        webTestClient
                .get()
                .uri(externalApiUrl)
                .headers(http -> http.setBearerAuth(jwtString))
                .exchange()

        then:
        0 * clientAuthorizationRequestService.getClientAuthorization(_) >> Optional.empty()
    }

    def 'given_token_with_clientId_should_call_clientAuthorizationRequestService_with_clientId'() {
        given:
        tokenContainsClientId("clientId1234")

        when:
        webTestClient
                .get()
                .uri(externalApiUrl)
                .headers(http -> http.setBearerAuth(jwtString))
                .exchange()

        then:
        1 * clientAuthorizationRequestService.getClientAuthorization(_) >> Optional.empty()
    }

    def 'given_no_token_should_return_unauthorized'() {
        when:
        WebTestClient.ResponseSpec responseSpec = webTestClient
                .get()
                .uri(externalApiUrl)
                .exchange()

        then:
        responseSpec.expectStatus().isUnauthorized()
    }

    def 'given_token_without_clientId_should_return_forbidden'() {
        given:
        tokenDoesNotContainClientId()

        when:
        WebTestClient.ResponseSpec responseSpec = webTestClient
                .get()
                .uri(externalApiUrl)
                .headers(http -> http.setBearerAuth(jwtString))
                .exchange()

        then:
        responseSpec.expectStatus().isForbidden()
    }

    def 'given_token_with_clientId_that_is_not_authorized_should_return_forbidden'() {
        given:
        tokenContainsClientId("clientId1234")
        clientIsNotAuthorized("clientId1234")

        when:
        WebTestClient.ResponseSpec responseSpec = webTestClient
                .get()
                .uri(externalApiUrl)
                .headers(http -> http.setBearerAuth(jwtString))
                .exchange()

        then:
        responseSpec.expectStatus().isForbidden()
    }

    def 'given_token_with_clientId_that_is_authorized_the_request_should_return_ok'() {
        given:
        tokenContainsClientId("clientId1234")
        clientIsAuthorized("clientId1234", "1")

        when:
        WebTestClient.ResponseSpec responseSpec = webTestClient
                .get()
                .uri(externalApiUrl)
                .headers(http -> http.setBearerAuth(jwtString))
                .exchange()

        then:
        responseSpec.expectStatus().isOk()
    }

    def 'given_token_with_clientId_but_no_empty_response_from_internal_authorization_request_should_return_forbidden'() {
        given:
        tokenContainsClientId("clientId1234")
        authorizationRequestReturnsEmpty("clientId1234")

        when:
        WebTestClient.ResponseSpec responseSpec = webTestClient
                .get()
                .uri(externalApiUrl)
                .headers(http -> http.setBearerAuth(jwtString))
                .exchange()

        then:
        responseSpec.expectStatus().isForbidden()
    }

}
