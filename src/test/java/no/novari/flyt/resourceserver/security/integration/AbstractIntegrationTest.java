package no.novari.flyt.resourceserver.security.integration;

import no.novari.cache.FintCache;
import no.novari.flyt.resourceserver.security.client.sourceapplication.SourceApplicationAuthorization;
import no.novari.flyt.resourceserver.security.client.sourceapplication.SourceApplicationAuthorizationRequestService;
import no.novari.flyt.resourceserver.security.integration.parameters.ExpectedResult;
import no.novari.flyt.resourceserver.security.integration.parameters.TestParameters;
import no.novari.flyt.resourceserver.security.integration.values.ClientId;
import no.novari.flyt.resourceserver.security.integration.values.PersonalTokenObjectIdentifier;
import no.novari.flyt.resourceserver.security.user.permission.UserPermission;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.support.JavaUtils;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
abstract class AbstractIntegrationTest {

    @MockitoBean
    private ReactiveJwtDecoder reactiveJwtDecoder;

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private SourceApplicationAuthorizationRequestService sourceApplicationAuthorizationRequestService;

    @MockitoBean(name = "userPermissionCachingListener")
    private ConcurrentMessageListenerContainer<String, UserPermission> userPermissionCachingListener;

    @MockitoBean
    private FintCache<UUID, UserPermission> userPermissionCache;

    @BeforeEach
    void setUp() {
        if (userPermissionCache != null) {
            mockUserPermission(
                    PersonalTokenObjectIdentifier.WITH_SA_1_2_AUTHORIZATIONS,
                    Set.of(1L, 2L)
            );
            mockUserPermission(
                    PersonalTokenObjectIdentifier.WITH_NO_SA_AUTHORIZATIONS,
                    Set.of()
            );
        }

        if (sourceApplicationAuthorizationRequestService != null) {
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

    public void performIntegrationTest(TestParameters testParameters) {
        Jwt token = testParameters.getTokenWrapper().getToken();

        if (token != null) {
            when(reactiveJwtDecoder.decode(token.getTokenValue()))
                    .thenReturn(Mono.just(
                            token
                    ));
        }

        ExpectedResult expectedResult = testParameters.getExpectedResult();

        Set<String> result = webTestClient
                .get()
                .uri(testParameters.getPath())
                .headers(http -> {
                    if (token != null) {
                        http.setBearerAuth(token.getTokenValue());
                    }
                })
                .exchange()
                .expectStatus().isEqualTo(expectedResult.getStatus())
                .returnResult(new ParameterizedTypeReference<Set<String>>() {
                })
                .getResponseBody().blockFirst();

        JavaUtils.INSTANCE.acceptIfNotNull(
                expectedResult.getAuthorities(),
                authorities -> assertThat(result).containsExactlyInAnyOrderElementsOf(authorities)
        );
    }

}
