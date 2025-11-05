package no.fintlabs.resourceserver.integration;

import no.fintlabs.cache.FintCache;
import no.fintlabs.resourceserver.UrlPaths;
import no.fintlabs.resourceserver.integration.utils.TokenWrapper;
import no.fintlabs.resourceserver.integration.utils.testValues.ClientId;
import no.fintlabs.resourceserver.integration.utils.testValues.PersonalTokenObjectIdentifier;
import no.fintlabs.resourceserver.integration.utils.testValues.PersonalTokenOrgId;
import no.fintlabs.resourceserver.security.SecurityConfiguration;
import no.fintlabs.resourceserver.security.client.sourceapplication.SourceApplicationAuthorization;
import no.fintlabs.resourceserver.security.client.sourceapplication.SourceApplicationAuthorizationRequestService;
import no.fintlabs.resourceserver.security.user.UserRole;
import no.fintlabs.resourceserver.security.user.userpermission.UserPermission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
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
import java.util.stream.Stream;

import static no.fintlabs.resourceserver.integration.utils.TokenFactory.createClientToken;
import static no.fintlabs.resourceserver.integration.utils.TokenFactory.createPersonalToken;
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

    public static Stream<IntegrationTestParameters> testParameters() {
        return Stream.of(
                // INTERNAL USER API
                new IntegrationTestParameters(
                        "Internal User API – No token",
                        UrlPaths.INTERNAL_API,
                        TokenWrapper.none(),
                        HttpStatus.UNAUTHORIZED
                ),
                new IntegrationTestParameters(
                        "Internal User API – User Role",
                        UrlPaths.INTERNAL_API,
                        createPersonalToken(
                                PersonalTokenOrgId.WITH_USER_ACCESS,
                                PersonalTokenObjectIdentifier.WITH_SA_1_2_AUTHORIZATIONS,
                                Set.of(UserRole.USER)
                        ),
                        HttpStatus.OK,
                        Set.of(
                                "ROLE_USER",
                                "SOURCE_APPLICATION_ID_1",
                                "SOURCE_APPLICATION_ID_2"
                        )),
                new IntegrationTestParameters(
                        "Internal User API – Admin Role",
                        UrlPaths.INTERNAL_API,
                        createPersonalToken(
                                PersonalTokenOrgId.WITH_USER_ACCESS,
                                PersonalTokenObjectIdentifier.WITH_SA_1_2_AUTHORIZATIONS,
                                Set.of(UserRole.ADMIN)
                        ),
                        HttpStatus.OK,
                        Set.of(
                                "ROLE_USER",
                                "ROLE_ADMIN",
                                "SOURCE_APPLICATION_ID_1",
                                "SOURCE_APPLICATION_ID_2"
                        )
                ),
                new IntegrationTestParameters(
                        "Internal User API – Developer Role",
                        UrlPaths.INTERNAL_API,
                        createPersonalToken(
                                PersonalTokenOrgId.WITH_USER_ACCESS,
                                PersonalTokenObjectIdentifier.WITH_SA_1_2_AUTHORIZATIONS,
                                Set.of(UserRole.DEVELOPER)
                        ),
                        HttpStatus.OK,
                        Set.of(
                                "ROLE_USER",
                                "ROLE_ADMIN",
                                "ROLE_DEVELOPER",
                                "SOURCE_APPLICATION_ID_1",
                                "SOURCE_APPLICATION_ID_2"
                        )),

                // INTERNAL ADMIN API
                new IntegrationTestParameters(
                        "Internal Admin API – No token",
                        UrlPaths.INTERNAL_ADMIN_API,
                        TokenWrapper.none(),
                        HttpStatus.UNAUTHORIZED),
                new IntegrationTestParameters(
                        "Internal Admin API – User Role",
                        UrlPaths.INTERNAL_ADMIN_API,
                        createPersonalToken(
                                PersonalTokenOrgId.WITH_USER_ACCESS,
                                PersonalTokenObjectIdentifier.WITH_SA_1_2_AUTHORIZATIONS,
                                Set.of(UserRole.USER)
                        ),
                        HttpStatus.FORBIDDEN),
                new IntegrationTestParameters(
                        "Internal Admin API – Admin Role",
                        UrlPaths.INTERNAL_ADMIN_API,
                        createPersonalToken(
                                PersonalTokenOrgId.WITH_USER_ACCESS,
                                PersonalTokenObjectIdentifier.WITH_SA_1_2_AUTHORIZATIONS,
                                Set.of(UserRole.ADMIN)
                        ),
                        HttpStatus.OK,
                        Set.of(
                                "ROLE_USER",
                                "ROLE_ADMIN",
                                "SOURCE_APPLICATION_ID_1",
                                "SOURCE_APPLICATION_ID_2"
                        )
                ),
                new IntegrationTestParameters(
                        "Internal Admin API – Developer Role",
                        UrlPaths.INTERNAL_ADMIN_API,
                        createPersonalToken(
                                PersonalTokenOrgId.WITH_USER_ACCESS,
                                PersonalTokenObjectIdentifier.WITH_SA_1_2_AUTHORIZATIONS,
                                Set.of(UserRole.DEVELOPER)
                        ),
                        HttpStatus.OK,
                        Set.of(
                                "ROLE_USER",
                                "ROLE_ADMIN",
                                "ROLE_DEVELOPER",
                                "SOURCE_APPLICATION_ID_1",
                                "SOURCE_APPLICATION_ID_2"
                        )
                ),

                // INTERNAL CLIENT API
                new IntegrationTestParameters(
                        "Internal Client API – No token",
                        UrlPaths.INTERNAL_CLIENT_API,
                        TokenWrapper.none(),
                        HttpStatus.UNAUTHORIZED
                ),
                new IntegrationTestParameters(
                        "Internal Client API – Valid Client Token",
                        UrlPaths.INTERNAL_CLIENT_API,
                        createClientToken(ClientId.AUTHORIZED_FOR_INTERNAL_API),
                        HttpStatus.OK
                ),
                new IntegrationTestParameters(
                        "Internal Client API – Invalid Client Token",
                        UrlPaths.INTERNAL_CLIENT_API,
                        createClientToken(ClientId.NOT_AUTHORIZED_FOR_INTERNAL_API),
                        HttpStatus.FORBIDDEN
                ),

                // TODO 04/11/2025 eivindmorch: External API

                // ACTUATOR ENDPOINTS
                new IntegrationTestParameters(
                        "Actuator API – No token",
                        "/actuator/",
                        TokenWrapper.none(),
                        HttpStatus.OK
                ),
                new IntegrationTestParameters(
                        "Actuator API – Valid Client Token",
                        "/actuator/",
                        createClientToken(ClientId.AUTHORIZED_FOR_INTERNAL_API),
                        HttpStatus.OK
                ),
                new IntegrationTestParameters(
                        "Actuator API – Invalid Client Token",
                        "/actuator/",
                        createClientToken(ClientId.NOT_AUTHORIZED_FOR_INTERNAL_API),
                        HttpStatus.OK
                ),

                // UNKNOWN API (global catch all chain)
                new IntegrationTestParameters(
                        "Unknown API – No token",
                        "/path/does/not/exist",
                        TokenWrapper.none(),
                        HttpStatus.UNAUTHORIZED
                ),
                new IntegrationTestParameters(
                        "Unknown API – Valid Client Token",
                        "/path/does/not/exist",
                        createClientToken(ClientId.AUTHORIZED_FOR_INTERNAL_API),
                        HttpStatus.UNAUTHORIZED
                ),
                new IntegrationTestParameters(
                        "Unknown API – Invalid Client Token",
                        "/path/does/not/exist",
                        createClientToken(ClientId.NOT_AUTHORIZED_FOR_INTERNAL_API),
                        HttpStatus.UNAUTHORIZED
                )
        );
    }

    @ParameterizedTest
    @MethodSource("testParameters")
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
                .uri(testParameters.getPath() + "/dummy")
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

        if (testParameters.expectedAuthorities != null) {
            assertThat(result).containsExactlyInAnyOrderElementsOf(testParameters.expectedAuthorities);
        }
    }

}
