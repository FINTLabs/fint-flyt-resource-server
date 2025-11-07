package no.novari.flyt.resourceserver.security.integration;

import no.fintlabs.cache.FintCache;
import no.novari.flyt.resourceserver.security.SecurityConfiguration;
import no.novari.flyt.resourceserver.security.client.sourceapplication.SourceApplicationAuthorization;
import no.novari.flyt.resourceserver.security.client.sourceapplication.SourceApplicationAuthorizationRequestService;
import no.novari.flyt.resourceserver.security.integration.parameters.IntegrationTestParameters;
import no.novari.flyt.resourceserver.security.integration.utils.testValues.ClientId;
import no.novari.flyt.resourceserver.security.integration.utils.testValues.PersonalTokenObjectIdentifier;
import no.novari.flyt.resourceserver.security.user.permission.UserPermission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.MethodSources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("all-api-enabled")
@Import({
        SecurityConfiguration.class,
        IntegrationTest.TestConfig.class
})
class IntegrationTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        ReactiveJwtDecoder reactiveJwtDecoder() {
            return mock(ReactiveJwtDecoder.class);
        }
    }

    @MockitoSpyBean
    private ReactiveJwtDecoder reactiveJwtDecoder;

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private SourceApplicationAuthorizationRequestService sourceApplicationAuthorizationRequestService;

    @MockitoBean
    private FintCache<UUID, UserPermission> userPermissionCache;

    @BeforeEach
    void setUp() {
        mockUserPermission(
                PersonalTokenObjectIdentifier.WITH_SA_1_2_AUTHORIZATIONS,
                Set.of(1L, 2L)
        );
        mockUserPermission(
                PersonalTokenObjectIdentifier.WITH_NO_SA_AUTHORIZATIONS,
                Set.of()
        );

        mockExternalClientSourceApplicationAuthorizations(
                ClientId.WITH_EXTERNAL_CLIENT_SA_AUTHORIZATION_ID_1,
                true,
                1L
        );
        mockExternalClientSourceApplicationAuthorizations(
                ClientId.WITH_NO_EXTERNAL_CLIENT_SA_AUTHORIZATION,
                false,
                null
        );
    }

    private void mockUserPermission(
            PersonalTokenObjectIdentifier objectIdentifier,
            Set<Long> sourceApplicationIds
    ) {
        when(userPermissionCache.getOptional(objectIdentifier.getUuid()))
                .thenReturn(Optional.of(
                        UserPermission
                                .builder()
                                .objectIdentifier(objectIdentifier.getUuid())
                                .sourceApplicationIds(sourceApplicationIds)
                                .build()
                ));
    }

    private void mockExternalClientSourceApplicationAuthorizations(
            ClientId clientId,
            boolean authorized,
            Long sourceApplicationId
    ) {
        when(sourceApplicationAuthorizationRequestService.getClientAuthorization(clientId.getClaimValue()))
                .thenReturn(Optional.of(
                        SourceApplicationAuthorization
                                .builder()
                                .authorized(authorized)
                                .clientId(clientId.getClaimValue())
                                .sourceApplicationId(sourceApplicationId)
                                .build()
                ));
    }

    @SuppressWarnings("JUnitMalformedDeclaration")
    @ParameterizedTest
    @MethodSources({
            @MethodSource("no.novari.flyt.resourceserver.security.integration.parameters.InternalUserApiTestParametersSource#generate"),
            @MethodSource("no.novari.flyt.resourceserver.security.integration.parameters.InternalAdminApiTestParametersSource#generate"),
            @MethodSource("no.novari.flyt.resourceserver.security.integration.parameters.InternalClientApiTestParametersSource#generate"),
            @MethodSource("no.novari.flyt.resourceserver.security.integration.parameters.ExternalClientApiTestParametersSource#generate"),
            @MethodSource("no.novari.flyt.resourceserver.security.integration.parameters.ActuatorApiTestParametersSource#generate"),
            @MethodSource("no.novari.flyt.resourceserver.security.integration.parameters.GlobalApiTestParametersSource#generate")
    })
    public void performIntegrationTest(IntegrationTestParameters testParameters) {
        Jwt token = testParameters.getTokenWrapper().getToken();

        if (token != null) {
            when(reactiveJwtDecoder.decode(token.getTokenValue()))
                    .thenReturn(Mono.just(
                            token
                    ));
        }

        Set<String> result = webTestClient
                .get()
                .uri(testParameters.getPath())
                .headers(http -> {
                    if (token != null) {
                        http.setBearerAuth(token.getTokenValue());
                    }
                })
                .exchange()
                .expectStatus().isEqualTo(testParameters.getExpectedResponseHttpStatus())
                .returnResult(new ParameterizedTypeReference<Set<String>>() {
                })
                .getResponseBody().blockFirst();

        if (testParameters.getExpectedAuthorities() != null) {
            assertThat(result).containsExactlyInAnyOrderElementsOf(testParameters.getExpectedAuthorities());
        }
    }

}
