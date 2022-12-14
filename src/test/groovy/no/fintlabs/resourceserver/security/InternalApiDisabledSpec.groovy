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

import static no.fintlabs.resourceserver.UrlPaths.INTERNAL_API
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity

@WebFluxTest(controllers = InternalApiTestController.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("external-api")
class InternalApiDisabledSpec extends Specification {

    @Autowired
    ApplicationContext applicationContext

    WebTestClient webTestClient

    @SpringBean
    private ReactiveJwtDecoder reactiveJwtDecoder = Mock(ReactiveJwtDecoder.class)

    @SpringBean
    SourceApplicationAuthorizationRequestService clientAuthorizationRequestService = Mock(SourceApplicationAuthorizationRequestService.class)

    private final String internalApiUrl = INTERNAL_API + "/dummy"

    private final String jwtString = "jwtString"

    def setup() {
        clientAuthorizationRequestService = Mock(SourceApplicationAuthorizationRequestService.class)
        webTestClient = WebTestClient
                .bindToApplicationContext(applicationContext)
                .apply(springSecurity())
                .build()
    }

    private void tokenContainsOrgId(String orgId) {
        reactiveJwtDecoder.decode(jwtString) >> Mono.just(new Jwt(
                jwtString,
                Instant.now(),
                Instant.now().plusMillis(20000),
                Map.of("header1", "header1"),
                Map.of(
                        "organizationid", orgId,
                        "organizationnumber", "organizationNumber",
                        "roles", []
                )
        ))
    }

    private void tokenDoesNotContainOrgId() {
        reactiveJwtDecoder.decode(jwtString) >> Mono.just(new Jwt(
                jwtString,
                Instant.now(),
                Instant.now().plusMillis(20000),
                Map.of("header1", "header1"),
                Map.of("claim1", "claim1")
        ))
    }

    def 'given_token_with_orgId_that_is_authorized_the_request_should_return_unauthorized'() {
        given:
        tokenContainsOrgId("example.no")

        when:
        WebTestClient.ResponseSpec responseSpec = webTestClient
                .get()
                .uri(internalApiUrl)
                .headers(http -> http.setBearerAuth(jwtString))
                .exchange()

        then:
        responseSpec.expectStatus().isUnauthorized()
    }

}
