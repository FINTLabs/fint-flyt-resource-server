package no.fintlabs.resourceserver.security


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

import static no.fintlabs.resourceserver.UrlPaths.INTERNAL_CLIENT_API
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity

@WebFluxTest(controllers = InternalClientApiTestController.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("internal-client-api")
class InternalClientApiEnabledSpec extends Specification {

    @Autowired
    ApplicationContext applicationContext

    WebTestClient webTestClient

    @SpringBean
    private ReactiveJwtDecoder reactiveJwtDecoder = Mock(ReactiveJwtDecoder.class)

    @SpringBean
    SourceApplicationAuthorizationRequestService clientAuthorizationRequestService = Mock(SourceApplicationAuthorizationRequestService.class)

    private final String internalClientApiUrl = INTERNAL_CLIENT_API + "/dummy"

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

    private void authorizationRequestReturnsEmpty(String clientId) {
        clientAuthorizationRequestService.getClientAuthorization(clientId) >> Optional.empty()
    }

    def 'given_no_token_should_return_unauthorized'() {
        when:
        WebTestClient.ResponseSpec responseSpec = webTestClient
                .get()
                .uri(internalClientApiUrl)
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
                .uri(internalClientApiUrl)
                .headers(http -> http.setBearerAuth(jwtString))
                .exchange()

        then:
        responseSpec.expectStatus().isForbidden()
    }

    def 'given_token_with_clientId_that_is_authorized_the_request_should_return_ok'() {
        given:
        tokenContainsClientId("1234")

        when:
        WebTestClient.ResponseSpec responseSpec = webTestClient
                .get()
                .uri(internalClientApiUrl)
                .headers(http -> http.setBearerAuth(jwtString))
                .exchange()

        then:
        responseSpec.expectStatus().isOk()
    }

    def 'given_token_with_clientId_that_is_not_authorized_the_request_should_return_ok'() {
        given:
        tokenContainsClientId("abcd")

        when:
        WebTestClient.ResponseSpec responseSpec = webTestClient
                .get()
                .uri(internalClientApiUrl)
                .headers(http -> http.setBearerAuth(jwtString))
                .exchange()

        then:
        responseSpec.expectStatus().isForbidden()
    }

}
