package no.fintlabs.resourceserver.security

import no.fintlabs.resourceserver.security.client.sourceapplication.SourceApplicationAuthorization
import no.fintlabs.resourceserver.security.client.sourceapplication.SourceApplicationAuthorizationRequestService
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

import static no.fintlabs.resourceserver.UrlPaths.EXTERNAL_API
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity

@WebFluxTest(controllers = ExternalApiTestController.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("internal-api")
class ExternalApiDisabledSpec extends Specification {

    @Autowired
    ApplicationContext applicationContext

    WebTestClient webTestClient

    @SpringBean
    private ReactiveJwtDecoder reactiveJwtDecoder = Mock(ReactiveJwtDecoder.class)

    @SpringBean
    SourceApplicationAuthorizationRequestService clientAuthorizationRequestService = Mock(SourceApplicationAuthorizationRequestService.class)

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
                SourceApplicationAuthorization
                        .builder()
                        .authorized(true)
                        .clientId(clientId)
                        .sourceApplicationId(sourceApplicationId)
                        .build()
        )
    }

    private void clientIsNotAuthorized(String clientId) {
        clientAuthorizationRequestService.getClientAuthorization(clientId) >> Optional.of(
                SourceApplicationAuthorization
                        .builder()
                        .authorized(false)
                        .clientId(clientId)
                        .build()
        )
    }

    private void authorizationRequestReturnsEmpty(String clientId) {
        clientAuthorizationRequestService.getClientAuthorization(clientId) >> Optional.empty()
    }

    def 'given_token_with_clientId_that_is_authorized_the_request_should_return_unauthorized'() {
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
        responseSpec.expectStatus().isUnauthorized()
    }

}
