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
import no.fintlabs.resourceserver.security.user.UserClaim;
import no.fintlabs.resourceserver.security.user.UserJwtConverter;
import no.fintlabs.resourceserver.security.user.UserRole;
import no.fintlabs.resourceserver.security.user.userpermission.UserPermission;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("all-api-enabled")
@Import({
        SecurityConfiguration.class,
//        InternalClientJwtConverter.class,
//        SourceApplicationJwtConverter.class,
//        UserJwtConverter.class,
////        SourceApplicationAuthorizationRequestService.class,
})
class EivindTesterLitt {

    @MockitoBean
    ReactiveJwtDecoder reactiveJwtDecoder;

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

    private static final String JWT_VALUE = "testJwtValue";

    private static final String SUB_CLAIM_NAME = "sub";

    private static final UUID VALID_USER_OBJECT_IDENTIFIER = UUID.fromString("a3be307e-e8d4-4475-8ed0-8d948dc47b86");
    private static final UUID INVALID_USER_OBJECT_IDENTIFIER = UUID.fromString("00000000-0000-0000-0000-0d0000000000");

    // TODO: Find better name for "valid"
    private static final String VALID_USER_ORG_ID = "domain-with-user-access.no";

    private static final String VALID_INTERNAL_CLIENT_SUB = "9e8118f3-9bc0-4f00-8675-c04bf8fe2494";
    private static final String VALID_EXTERNAL_CLIENT_SUB = "2aa733b0-1602-4527-be3c-1e6dd05a6c58";
    private static final String INVALID_CLIENT_SUB = "3d416475-0b4b-473a-b5a2-7742f5e68391";

    enum TokenType {
        NO_TOKEN,
        PERSONAL_TOKEN,
        PERSONAL_TOKEN_WITH_ORG_ID,
        PERSONAL_TOKEN_WITH_ORG_ID_AND_OBJ_ID,
        PERSONAL_TOKEN_WITH_ORG_ID_AND_OBJ_ID_AND_USER_ROLE,
        PERSONAL_TOKEN_WITH_ORG_ID_AND_OBJ_ID_AND_DEVELOPER_ROLE,
        PERSONAL_TOKEN_WITH_ORG_ID_AND_OBJ_ID_AND_ADMIN_ROLE,
        CLIENT_TOKEN_WITH_VALID_INTERNAL_CLIENT_ID,
        CLIENT_TOKEN_WITH_INVALID_CLIENT_ID,
        CLIENT_TOKEN_WITH_VALID_EXTERNAL_CLIENT_ID,
    }


    public void mockJwtDecoder(TokenType tokenType) {
        if (tokenType == TokenType.NO_TOKEN) {
            return;
        }
        when(reactiveJwtDecoder.decode(JWT_VALUE)).thenReturn(Mono.just(createJwt(createClaims(tokenType))));
    }

    // TODO: Rename invalid to unauthorized
    private Map<String, Object> createClaims(TokenType tokenType) {
        return switch (tokenType) {
            case NO_TOKEN -> null;
            case PERSONAL_TOKEN -> Map.of();
            case PERSONAL_TOKEN_WITH_ORG_ID -> Map.of(
                    UserClaim.ORGANIZATION_ID.getJwtTokenClaimName(), VALID_USER_ORG_ID);
            case PERSONAL_TOKEN_WITH_ORG_ID_AND_OBJ_ID -> Map.of(
                    UserClaim.ORGANIZATION_ID.getJwtTokenClaimName(), VALID_USER_ORG_ID,
                    UserClaim.OBJECT_IDENTIFIER.getJwtTokenClaimName(), VALID_USER_OBJECT_IDENTIFIER
            );
            case PERSONAL_TOKEN_WITH_ORG_ID_AND_OBJ_ID_AND_USER_ROLE -> Map.of(
                    UserClaim.ORGANIZATION_ID.getJwtTokenClaimName(), VALID_USER_ORG_ID,
                    UserClaim.OBJECT_IDENTIFIER.getJwtTokenClaimName(), VALID_USER_OBJECT_IDENTIFIER,
                    UserClaim.ROLES.getJwtTokenClaimName(), List.of(UserRole.USER.getClaimValue())
            );
            case PERSONAL_TOKEN_WITH_ORG_ID_AND_OBJ_ID_AND_DEVELOPER_ROLE -> Map.of(
                    UserClaim.ORGANIZATION_ID.getJwtTokenClaimName(), VALID_USER_ORG_ID,
                    UserClaim.OBJECT_IDENTIFIER.getJwtTokenClaimName(), VALID_USER_OBJECT_IDENTIFIER,
                    UserClaim.ROLES.getJwtTokenClaimName(), List.of(UserRole.DEVELOPER.getClaimValue())
            );
            case PERSONAL_TOKEN_WITH_ORG_ID_AND_OBJ_ID_AND_ADMIN_ROLE -> Map.of(
                    UserClaim.ORGANIZATION_ID.getJwtTokenClaimName(), VALID_USER_ORG_ID,
                    UserClaim.OBJECT_IDENTIFIER.getJwtTokenClaimName(), VALID_USER_OBJECT_IDENTIFIER,
                    UserClaim.ROLES.getJwtTokenClaimName(), List.of(UserRole.ADMIN.getClaimValue())
            );
            case CLIENT_TOKEN_WITH_VALID_INTERNAL_CLIENT_ID -> Map.of(
                    SUB_CLAIM_NAME, VALID_INTERNAL_CLIENT_SUB
            );
            case CLIENT_TOKEN_WITH_VALID_EXTERNAL_CLIENT_ID -> Map.of(
                    SUB_CLAIM_NAME, VALID_EXTERNAL_CLIENT_SUB
            );
            case CLIENT_TOKEN_WITH_INVALID_CLIENT_ID -> Map.of(
                    SUB_CLAIM_NAME, INVALID_CLIENT_SUB
            );
        };
    }

    private Jwt createJwt(Map<String, Object> claims) {
        return new Jwt(
                JWT_VALUE,
                Instant.now(),
                Instant.now().plusMillis(20000),
                Map.of("header1", "header1"),
                claims
        );
    }

    @Getter
    @ToString
    @Builder
    public static class TestParameters {
        String description;
        @Builder.Default
        Map<UUID, List<Long>> userPermissions = Map.of();
        @Builder.Default
        Map<String, Long> sourceApplicationAuthorizations = Map.of();
        String path;
        TokenType tokenType;
        HttpStatus expectedResponseHttpStatus;
        Set<String> expectedAuthorities;
    }

    public static Stream<TestParameters> testParameters() {
        return Stream.of(
//                // INTERNAL API
//                TestParameters
//                        .builder()
//                        .description("Internal API – No token")
//                        .path(UrlPaths.INTERNAL_API)
//                        .tokenType(TokenType.NO_TOKEN)
//                        .expectedResponseHttpStatus(HttpStatus.UNAUTHORIZED)
//                        .build(),
//                TestParameters
//                        .builder()
//                        .description("Internal API – User Role")
//                        .userPermissions(Map.of(
//                                VALID_USER_OBJECT_IDENTIFIER,
//                                List.of(1L, 2L)
//                        ))
//                        .sourceApplicationAuthorizations(Map.of())
//                        .path(UrlPaths.INTERNAL_API)
//                        .tokenType(TokenType.PERSONAL_TOKEN_WITH_ORG_ID_AND_OBJ_ID_AND_USER_ROLE)
//                        .expectedResponseHttpStatus(HttpStatus.OK)
//                        .expectedAuthorities(Set.of(
//                                "ROLE_USER",
//                                "SOURCE_APPLICATION_ID_1",
//                                "SOURCE_APPLICATION_ID_2"
//                        ))
//                        .build(),
//                TestParameters
//                        .builder()
//                        .description("Internal API – Developer Role")
//                        .userPermissions(Map.of(
//                                VALID_USER_OBJECT_IDENTIFIER,
//                                List.of(1L, 2L)
//                        ))
//                        .sourceApplicationAuthorizations(Map.of())
//                        .path(UrlPaths.INTERNAL_API)
//                        .tokenType(TokenType.PERSONAL_TOKEN_WITH_ORG_ID_AND_OBJ_ID_AND_DEVELOPER_ROLE)
//                        .expectedResponseHttpStatus(HttpStatus.OK)
//                        .expectedAuthorities(Set.of(
//                                "ROLE_DEVELOPER",
//                                "SOURCE_APPLICATION_ID_1",
//                                "SOURCE_APPLICATION_ID_2"
//                        ))
//                        .build(),
//                TestParameters
//                        .builder()
//                        .description("Internal API – Admin Role")
//                        .userPermissions(Map.of(
//                                VALID_USER_OBJECT_IDENTIFIER,
//                                List.of(1L, 2L)
//                        ))
//                        .sourceApplicationAuthorizations(Map.of())
//                        .path(UrlPaths.INTERNAL_ADMIN_API)
//                        .tokenType(TokenType.PERSONAL_TOKEN_WITH_ORG_ID_AND_OBJ_ID_AND_ADMIN_ROLE)
//                        .expectedResponseHttpStatus(HttpStatus.OK)
//                        .expectedAuthorities(Set.of(
//                                "ROLE_ADMIN",
//                                "SOURCE_APPLICATION_ID_1",
//                                "SOURCE_APPLICATION_ID_2"
//                        ))
//                        .build(),
//
//                // ADMIN API
//                TestParameters
//                        .builder()
//                        .description("Admin API – No token")
//                        .path(UrlPaths.INTERNAL_ADMIN_API)
//                        .tokenType(TokenType.NO_TOKEN)
//                        .expectedResponseHttpStatus(HttpStatus.UNAUTHORIZED)
//                        .build(),
//                TestParameters
//                        .builder()
//                        .description("Admin API – User Role")
//                        .userPermissions(Map.of(
//                                VALID_USER_OBJECT_IDENTIFIER,
//                                List.of(1L, 2L)
//                        ))
//                        .sourceApplicationAuthorizations(Map.of())
//                        .path(UrlPaths.INTERNAL_ADMIN_API)
//                        .tokenType(TokenType.PERSONAL_TOKEN_WITH_ORG_ID_AND_OBJ_ID_AND_USER_ROLE)
//                        .expectedResponseHttpStatus(HttpStatus.FORBIDDEN)
//                        .build(),
//                TestParameters
//                        .builder()
//                        .description("Admin API – Developer Role")
//                        .userPermissions(Map.of(
//                                VALID_USER_OBJECT_IDENTIFIER,
//                                List.of(1L, 2L)
//                        ))
//                        .sourceApplicationAuthorizations(Map.of())
//                        .path(UrlPaths.INTERNAL_ADMIN_API)
//                        .tokenType(TokenType.PERSONAL_TOKEN_WITH_ORG_ID_AND_OBJ_ID_AND_DEVELOPER_ROLE)
//                        .expectedResponseHttpStatus(HttpStatus.OK)
//                        .expectedAuthorities(Set.of(
//                                "ROLE_DEVELOPER",
//                                "SOURCE_APPLICATION_ID_1",
//                                "SOURCE_APPLICATION_ID_2"
//                        ))
//                        .build(),
//                TestParameters
//                        .builder()
//                        .description("Admin API – Admin Role")
//                        .userPermissions(Map.of(
//                                VALID_USER_OBJECT_IDENTIFIER,
//                                List.of(1L, 2L)
//                        ))
//                        .sourceApplicationAuthorizations(Map.of())
//                        .path(UrlPaths.INTERNAL_ADMIN_API)
//                        .tokenType(TokenType.PERSONAL_TOKEN_WITH_ORG_ID_AND_OBJ_ID_AND_ADMIN_ROLE)
//                        .expectedResponseHttpStatus(HttpStatus.OK)
//                        .expectedAuthorities(Set.of(
//                                "ROLE_ADMIN",
//                                "SOURCE_APPLICATION_ID_1",
//                                "SOURCE_APPLICATION_ID_2"
//                        ))
//                        .build(),
//
//                // INTERNAL CLIENT API
//                TestParameters
//                        .builder()
//                        .description("Internal Client API – No token")
//                        .path(UrlPaths.INTERNAL_CLIENT_API)
//                        .tokenType(TokenType.NO_TOKEN)
//                        .expectedResponseHttpStatus(HttpStatus.UNAUTHORIZED)
//                        .build(),
//                TestParameters
//                        .builder()
//                        .description("Internal Client API – Valid Client Token")
//                        .path(UrlPaths.INTERNAL_CLIENT_API)
//                        .tokenType(TokenType.CLIENT_TOKEN_WITH_VALID_INTERNAL_CLIENT_ID)
//                        .expectedResponseHttpStatus(HttpStatus.OK)
//                        .build(),
//                TestParameters
//                        .builder()
//                        .description("Internal Client API – Invalid Client Token")
//                        .path(UrlPaths.INTERNAL_CLIENT_API)
//                        .tokenType(TokenType.CLIENT_TOKEN_WITH_INVALID_CLIENT_ID)
//                        .expectedResponseHttpStatus(HttpStatus.FORBIDDEN)
//                        .build(),
//
//                // EXTERNAL CLIENT API
//                TestParameters
//                        .builder()
//                        .description("External API – No token")
//                        .path(UrlPaths.EXTERNAL_API)
//                        .tokenType(TokenType.NO_TOKEN)
//                        .expectedResponseHttpStatus(HttpStatus.UNAUTHORIZED)
//                        .build(),
                TestParameters
                        .builder()
                        .description("External API – Valid Client Token")
                        .path(UrlPaths.EXTERNAL_API)
                        .tokenType(TokenType.CLIENT_TOKEN_WITH_VALID_INTERNAL_CLIENT_ID)
                        .expectedResponseHttpStatus(HttpStatus.OK)
                        .sourceApplicationAuthorizations(Map.of(VALID_EXTERNAL_CLIENT_SUB, 1L))
                        .build(),
                TestParameters
                        .builder()
                        .description("External API – Invalid Client Token")
                        .path(UrlPaths.EXTERNAL_API)
                        .tokenType(TokenType.CLIENT_TOKEN_WITH_INVALID_CLIENT_ID)
                        .expectedResponseHttpStatus(HttpStatus.FORBIDDEN)
                        .build()
        );
    }

    @ParameterizedTest(name = "{index} => {0}")
    @MethodSource("testParameters")
    public void doSomething(TestParameters testParameters) {
        mockJwtDecoder(testParameters.getTokenType());

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

        List<String> result = webTestClient
                .get()
                .uri(testParameters.getPath() + "/dummy")
                .headers(http -> {
                    if (testParameters.getTokenType() != TokenType.NO_TOKEN) {
                        http.setBearerAuth(JWT_VALUE);
                    }
                })
                .exchange()
                .expectStatus().isEqualTo(testParameters.getExpectedResponseHttpStatus())
                .returnResult(new ParameterizedTypeReference<List<String>>() {
                })
                .getResponseBody().blockFirst();

        if (testParameters.expectedAuthorities != null) {
            assertThat(result).containsExactlyInAnyOrderElementsOf(testParameters.expectedAuthorities);
        }
    }

}
