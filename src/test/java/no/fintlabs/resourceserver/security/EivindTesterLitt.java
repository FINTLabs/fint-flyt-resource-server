package no.fintlabs.resourceserver.security;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import no.fintlabs.cache.FintCache;
import no.fintlabs.resourceserver.UrlPaths;
import no.fintlabs.resourceserver.security.client.InternalClientJwtConverter;
import no.fintlabs.resourceserver.security.client.sourceapplication.SourceApplicationAuthorization;
import no.fintlabs.resourceserver.security.client.sourceapplication.SourceApplicationAuthorizationRequestService;
import no.fintlabs.resourceserver.security.client.sourceapplication.SourceApplicationJwtConverter;
import no.fintlabs.resourceserver.security.user.UserJwtConverter;
import no.fintlabs.resourceserver.security.user.userpermission.UserPermission;
import no.fintlabs.resourceserver.testutils.JwtFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.*;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("all-api-enabled")
@Import({
        SecurityConfiguration.class,
        InternalClientJwtConverter.class,
        SourceApplicationJwtConverter.class,
        UserJwtConverter.class
})
class EivindTesterLitt {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    SourceApplicationAuthorizationRequestService sourceApplicationAuthorizationRequestService;

    @MockitoBean
    FintCache<UUID, UserPermission> userPermissionCache;

    // TODO 27/10/2025 eivindmorch: Params:
    //  STATE
    //      – Application properties
    //          – Internal
    //              – Role filter
    //          – Internal client
    //              – Authorized client ids
    //          – External client
    //              – Authorized client ids
    //      – User permissions
    //      – Source application authorization
    //  REQUEST
    //      – Path
    //          – Internal user
    //          – Internal client
    //          – External client
    //      – Token
    //          – None
    //          – User
    //              – No org
    //              – Org, no object identifier
    //              – Org, object identifier, no roles
    //              – Org, object identifier, roles
    //          – Client
    //              – No client id
    //              – Client id
    //  EXPECTED OUTCOME
    //      – Forbidden
    //      – Unauthorized
    //      – Authorized
    //          – Authorities
    //              – Roles
    //              – Source application id(s)

    @Getter
    @ToString
    @Builder
    public static class TestParameters {
        Map<UUID, List<Long>> userPermissions;
        Map<String, Long> sourceApplicationAuthorizations;
        String path;
        Jwt jwt;
        HttpStatus expectedResponseHttpStatus;
        Set<String> expectedAuthorities;
    }

    public static Stream<TestParameters> testParameters() {
        return Stream.of(
                TestParameters
                        .builder()
                        .userPermissions(
                                Map.of(
                                        UUID.fromString("a3be307e-e8d4-4475-8ed0-8d948dc47b86"),
                                        List.of(1L, 2L)
                                )
                        )
                        .sourceApplicationAuthorizations(Map.of())
                        .path(UrlPaths.INTERNAL_API)
                        .jwt(JwtFactory.createEndUserJwt())
                        .expectedResponseHttpStatus(HttpStatus.OK)
                        .expectedAuthorities(Set.of(
                                "ROLE_USER",
                                "SOURCE_APPLICATION_ID_1",
                                "SOURCE_APPLICATION_ID_2"
                        ))
                        .build()
        );
    }

    @ParameterizedTest
    @MethodSource("testParameters")
    public void doSomething(TestParameters testParameters) {
        when(sourceApplicationAuthorizationRequestService.getClientAuthorization(anyString()))
                .thenAnswer(invocation -> {
                            String clientId = invocation.getArgument(0);
                            return Optional.ofNullable(
                                    testParameters.sourceApplicationAuthorizations.getOrDefault(
                                            clientId,
                                            null
                                    )
                            ).map(id -> SourceApplicationAuthorization
                                    .builder()
                                    .authorized(true)
                                    .clientId(clientId)
                                    .sourceApplicationId(id)
                                    .build()
                            );
                        }
                );

        when(userPermissionCache.getOptional(any(UUID.class)))
                .thenAnswer(invocation -> {
                    UUID objectIdentifier = invocation.getArgument(0);
                    return Optional.ofNullable(
                            testParameters.getUserPermissions()
                                    .getOrDefault(objectIdentifier, null)
                    ).map(sourceApplicationIds -> UserPermission
                            .builder()
                            .objectIdentifier(objectIdentifier)
                            .sourceApplicationIds(sourceApplicationIds)
                            .build()
                    );
                });

        webTestClient
                .get()
                .uri(testParameters.getPath() + "/dummy")
                .headers(http -> http.setBearerAuth(testParameters.getJwt().getTokenValue()))
                .exchange()
                .expectStatus().isEqualTo(testParameters.getExpectedResponseHttpStatus());
    }

}
