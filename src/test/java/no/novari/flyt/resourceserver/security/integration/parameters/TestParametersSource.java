package no.novari.flyt.resourceserver.security.integration.parameters;

import no.novari.flyt.resourceserver.UrlPaths;
import no.novari.flyt.resourceserver.security.integration.values.ClientId;
import no.novari.flyt.resourceserver.security.integration.values.PersonalTokenObjectIdentifier;
import no.novari.flyt.resourceserver.security.integration.values.PersonalTokenOrgId;
import no.novari.flyt.resourceserver.security.user.UserRole;
import org.springframework.http.HttpStatus;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static no.novari.flyt.resourceserver.security.integration.parameters.TestParametersCategory.*;
import static no.novari.flyt.resourceserver.security.integration.parameters.TokenFactory.createClientToken;
import static no.novari.flyt.resourceserver.security.integration.parameters.TokenFactory.createPersonalToken;

public class TestParametersSource {

    public static Stream<TestParameters> internalApiEnabled() {
        return Stream.concat(
                testParametersPerApiAndToggleStatus.get(INTERNAL_USER_API_IF_INTERNAL_API_ENABLED).stream(),
                testParametersPerApiAndToggleStatus.get(INTERNAL_ADMIN_API_IF_INTERNAL_API_ENABLED).stream()
        );
    }

    public static Stream<TestParameters> internalApiDisabled() {
        return Stream.concat(
                testParametersPerApiAndToggleStatus.get(INTERNAL_USER_API_IF_INTERNAL_API_DISABLED).stream(),
                testParametersPerApiAndToggleStatus.get(INTERNAL_ADMIN_API_IF_INTERNAL_API_DISABLED).stream()
        );
    }

    public static Stream<TestParameters> internalClientApiEnabled() {
        return testParametersPerApiAndToggleStatus.get(INTERNAL_CLIENT_API_IF_ENABLED).stream();
    }

    public static Stream<TestParameters> internalClientApiDisabled() {
        return testParametersPerApiAndToggleStatus.get(INTERNAL_CLIENT_API_IF_DISABLED).stream();
    }

    public static Stream<TestParameters> externalClientApiEnabled() {
        return testParametersPerApiAndToggleStatus.get(EXTERNAL_CLIENT_API_IF_ENABLED).stream();
    }

    public static Stream<TestParameters> externalClientApiDisabled() {
        return testParametersPerApiAndToggleStatus.get(EXTERNAL_CLIENT_API_IF_DISABLED).stream();
    }

    public static Stream<TestParameters> actuator() {
        return testParametersPerApiAndToggleStatus.get(ACTUATOR).stream();
    }

    public static Stream<TestParameters> global() {
        return testParametersPerApiAndToggleStatus.get(GLOBAL).stream();
    }

    private static final Map<TestParametersCategory, List<TestParameters>> testParametersPerApiAndToggleStatus =
            createTokensWithExpectedResultPerApiAndToggleStatus()
                    .flatMap(tokenWithExpectedResultPerApiAndToggleStatus ->
                            Arrays.stream(TestParametersCategory.values())
                                    .map(testParametersCategory -> {
                                        ExpectedResult expectedResult =
                                                tokenWithExpectedResultPerApiAndToggleStatus.getT2()
                                                        .apply(testParametersCategory);
                                        return Tuples.of(
                                                testParametersCategory,
                                                new TestParameters(
                                                        getPathForApi(testParametersCategory),
                                                        tokenWithExpectedResultPerApiAndToggleStatus.getT1(),
                                                        expectedResult
                                                )
                                        );
                                    })
                    ).collect(Collectors.groupingBy(
                                    Tuple2::getT1,
                                    Collectors.mapping(Tuple2::getT2, Collectors.toList())
                            )
                    );

    private static String getPathForApi(TestParametersCategory apiTestCaseCondition) {
        return switch (apiTestCaseCondition) {
            case INTERNAL_USER_API_IF_INTERNAL_API_ENABLED, INTERNAL_USER_API_IF_INTERNAL_API_DISABLED ->
                    UrlPaths.INTERNAL_API + "/dummy";
            case INTERNAL_ADMIN_API_IF_INTERNAL_API_ENABLED, INTERNAL_ADMIN_API_IF_INTERNAL_API_DISABLED ->
                    UrlPaths.INTERNAL_ADMIN_API + "/dummy";
            case INTERNAL_CLIENT_API_IF_ENABLED, INTERNAL_CLIENT_API_IF_DISABLED ->
                    UrlPaths.INTERNAL_CLIENT_API + "/dummy";
            case EXTERNAL_CLIENT_API_IF_ENABLED, EXTERNAL_CLIENT_API_IF_DISABLED -> UrlPaths.EXTERNAL_API + "/dummy";
            case GLOBAL -> "/not/matching/any/filter";
            case ACTUATOR -> "/actuator/dummy";
        };
    }

    private static Stream<Tuple2<TokenWrapper, Function<TestParametersCategory, ExpectedResult>>>
    createTokensWithExpectedResultPerApiAndToggleStatus() {
        return Stream.of(
                Tuples.of(
                        TokenWrapper.none(),
                        testParametersCategory -> testParametersCategory == ACTUATOR
                                ? new ExpectedResult(HttpStatus.OK)
                                : new ExpectedResult(HttpStatus.UNAUTHORIZED)
                ),
                Tuples.of(
                        createPersonalToken(
                                PersonalTokenOrgId.WITH_NO_ACCESS,
                                PersonalTokenObjectIdentifier.WITH_NO_USER_PERMISSION,
                                Set.of()
                        ),
                        testParametersCategory -> switch (testParametersCategory) {
                            case INTERNAL_USER_API_IF_INTERNAL_API_ENABLED,
                                 INTERNAL_ADMIN_API_IF_INTERNAL_API_ENABLED -> new ExpectedResult(HttpStatus.FORBIDDEN);
                            case ACTUATOR -> new ExpectedResult(HttpStatus.OK);
                            default -> new ExpectedResult(HttpStatus.UNAUTHORIZED);
                        }
                ),
                Tuples.of(
                        createPersonalToken(
                                PersonalTokenOrgId.WITH_ALL_USER_ACCESS,
                                PersonalTokenObjectIdentifier.WITH_SA_1_2_AUTHORIZATIONS,
                                Set.of()
                        ),
                        testParametersCategory -> switch (testParametersCategory) {
                            case INTERNAL_USER_API_IF_INTERNAL_API_ENABLED,
                                 INTERNAL_ADMIN_API_IF_INTERNAL_API_ENABLED -> new ExpectedResult(HttpStatus.FORBIDDEN);
                            case ACTUATOR -> new ExpectedResult(HttpStatus.OK);
                            default -> new ExpectedResult(HttpStatus.UNAUTHORIZED);
                        }
                ),


                Tuples.of(
                        createPersonalToken(
                                PersonalTokenOrgId.WITH_NO_ACCESS,
                                PersonalTokenObjectIdentifier.WITH_NO_USER_PERMISSION,
                                Set.of(UserRole.USER)
                        ),
                        testParametersCategory -> switch (testParametersCategory) {
                            case INTERNAL_USER_API_IF_INTERNAL_API_ENABLED,
                                 INTERNAL_ADMIN_API_IF_INTERNAL_API_ENABLED -> new ExpectedResult(HttpStatus.FORBIDDEN);
                            case ACTUATOR -> new ExpectedResult(HttpStatus.OK);
                            default -> new ExpectedResult(HttpStatus.UNAUTHORIZED);
                        }
                ),
                Tuples.of(
                        createPersonalToken(
                                PersonalTokenOrgId.WITH_ALL_USER_ACCESS,
                                PersonalTokenObjectIdentifier.WITH_NO_USER_PERMISSION,
                                Set.of(UserRole.USER)
                        ),
                        testParametersCategory -> switch (testParametersCategory) {
                            case INTERNAL_USER_API_IF_INTERNAL_API_ENABLED -> new ExpectedResult(
                                    HttpStatus.OK,
                                    Set.of("ROLE_USER")
                            );
                            case INTERNAL_ADMIN_API_IF_INTERNAL_API_ENABLED -> new ExpectedResult(HttpStatus.FORBIDDEN);
                            case ACTUATOR -> new ExpectedResult(HttpStatus.OK);
                            default -> new ExpectedResult(HttpStatus.UNAUTHORIZED);
                        }
                ),
                Tuples.of(
                        createPersonalToken(
                                PersonalTokenOrgId.WITH_ALL_USER_ACCESS,
                                PersonalTokenObjectIdentifier.WITH_NO_SA_AUTHORIZATIONS,
                                Set.of(UserRole.USER)
                        ),
                        testParametersCategory -> switch (testParametersCategory) {
                            case INTERNAL_USER_API_IF_INTERNAL_API_ENABLED -> new ExpectedResult(
                                    HttpStatus.OK,
                                    Set.of("ROLE_USER")
                            );
                            case INTERNAL_ADMIN_API_IF_INTERNAL_API_ENABLED -> new ExpectedResult(HttpStatus.FORBIDDEN);
                            case ACTUATOR -> new ExpectedResult(HttpStatus.OK);
                            default -> new ExpectedResult(HttpStatus.UNAUTHORIZED);
                        }
                ),
                Tuples.of(
                        createPersonalToken(
                                PersonalTokenOrgId.WITH_ALL_USER_ACCESS,
                                PersonalTokenObjectIdentifier.WITH_SA_1_2_AUTHORIZATIONS,
                                Set.of(UserRole.USER)
                        ),
                        testParametersCategory -> switch (testParametersCategory) {
                            case INTERNAL_USER_API_IF_INTERNAL_API_ENABLED -> new ExpectedResult(
                                    HttpStatus.OK,
                                    Set.of("ROLE_USER", "SOURCE_APPLICATION_ID_1", "SOURCE_APPLICATION_ID_2")
                            );
                            case INTERNAL_ADMIN_API_IF_INTERNAL_API_ENABLED -> new ExpectedResult(HttpStatus.FORBIDDEN);
                            case ACTUATOR -> new ExpectedResult(HttpStatus.OK);
                            default -> new ExpectedResult(HttpStatus.UNAUTHORIZED);
                        }
                ),
                Tuples.of(
                        createPersonalToken(
                                PersonalTokenOrgId.WITH_ADMIN_AND_DEV_ACCESS,
                                PersonalTokenObjectIdentifier.WITH_SA_1_2_AUTHORIZATIONS,
                                Set.of(UserRole.USER)
                        ),
                        testParametersCategory -> switch (testParametersCategory) {
                            case INTERNAL_USER_API_IF_INTERNAL_API_ENABLED,
                                 INTERNAL_ADMIN_API_IF_INTERNAL_API_ENABLED -> new ExpectedResult(HttpStatus.FORBIDDEN);
                            case ACTUATOR -> new ExpectedResult(HttpStatus.OK);
                            default -> new ExpectedResult(HttpStatus.UNAUTHORIZED);
                        }
                ),


                Tuples.of(
                        createPersonalToken(
                                PersonalTokenOrgId.WITH_NO_ACCESS,
                                PersonalTokenObjectIdentifier.WITH_NO_USER_PERMISSION,
                                Set.of(UserRole.ADMIN)
                        ),
                        testParametersCategory -> switch (testParametersCategory) {
                            case INTERNAL_USER_API_IF_INTERNAL_API_ENABLED,
                                 INTERNAL_ADMIN_API_IF_INTERNAL_API_ENABLED -> new ExpectedResult(HttpStatus.FORBIDDEN);
                            case ACTUATOR -> new ExpectedResult(HttpStatus.OK);
                            default -> new ExpectedResult(HttpStatus.UNAUTHORIZED);
                        }
                ),
                Tuples.of(
                        createPersonalToken(
                                PersonalTokenOrgId.WITH_ALL_USER_ACCESS,
                                PersonalTokenObjectIdentifier.WITH_NO_USER_PERMISSION,
                                Set.of(UserRole.ADMIN)
                        ),
                        testParametersCategory -> switch (testParametersCategory) {
                            case INTERNAL_USER_API_IF_INTERNAL_API_ENABLED,
                                 INTERNAL_ADMIN_API_IF_INTERNAL_API_ENABLED -> new ExpectedResult(
                                    HttpStatus.OK,
                                    Set.of("ROLE_USER", "ROLE_ADMIN")
                            );
                            case ACTUATOR -> new ExpectedResult(HttpStatus.OK);
                            default -> new ExpectedResult(HttpStatus.UNAUTHORIZED);
                        }
                ),
                Tuples.of(
                        createPersonalToken(
                                PersonalTokenOrgId.WITH_ALL_USER_ACCESS,
                                PersonalTokenObjectIdentifier.WITH_NO_SA_AUTHORIZATIONS,
                                Set.of(UserRole.ADMIN)
                        ),
                        testParametersCategory -> switch (testParametersCategory) {
                            case INTERNAL_USER_API_IF_INTERNAL_API_ENABLED,
                                 INTERNAL_ADMIN_API_IF_INTERNAL_API_ENABLED -> new ExpectedResult(
                                    HttpStatus.OK,
                                    Set.of("ROLE_USER", "ROLE_ADMIN")
                            );
                            case ACTUATOR -> new ExpectedResult(HttpStatus.OK);
                            default -> new ExpectedResult(HttpStatus.UNAUTHORIZED);
                        }
                ),
                Tuples.of(
                        createPersonalToken(
                                PersonalTokenOrgId.WITH_ALL_USER_ACCESS,
                                PersonalTokenObjectIdentifier.WITH_SA_1_2_AUTHORIZATIONS,
                                Set.of(UserRole.ADMIN)
                        ),
                        testParametersCategory -> switch (testParametersCategory) {
                            case INTERNAL_USER_API_IF_INTERNAL_API_ENABLED,
                                 INTERNAL_ADMIN_API_IF_INTERNAL_API_ENABLED -> new ExpectedResult(
                                    HttpStatus.OK,
                                    Set.of(
                                            "ROLE_USER", "ROLE_ADMIN",
                                            "SOURCE_APPLICATION_ID_1", "SOURCE_APPLICATION_ID_2"
                                    )
                            );
                            case ACTUATOR -> new ExpectedResult(HttpStatus.OK);
                            default -> new ExpectedResult(HttpStatus.UNAUTHORIZED);
                        }
                ),
                Tuples.of(
                        createPersonalToken(
                                PersonalTokenOrgId.WITH_ADMIN_AND_DEV_ACCESS,
                                PersonalTokenObjectIdentifier.WITH_SA_1_2_AUTHORIZATIONS,
                                Set.of(UserRole.ADMIN)
                        ),
                        testParametersCategory -> switch (testParametersCategory) {
                            case INTERNAL_USER_API_IF_INTERNAL_API_ENABLED,
                                 INTERNAL_ADMIN_API_IF_INTERNAL_API_ENABLED -> new ExpectedResult(
                                    HttpStatus.OK,
                                    Set.of(
                                            "ROLE_USER", "ROLE_ADMIN",
                                            "SOURCE_APPLICATION_ID_1", "SOURCE_APPLICATION_ID_2"
                                    )
                            );
                            case ACTUATOR -> new ExpectedResult(HttpStatus.OK);
                            default -> new ExpectedResult(HttpStatus.UNAUTHORIZED);
                        }
                ),


                Tuples.of(
                        createPersonalToken(
                                PersonalTokenOrgId.WITH_NO_ACCESS,
                                PersonalTokenObjectIdentifier.WITH_NO_USER_PERMISSION,
                                Set.of(UserRole.DEVELOPER)
                        ),
                        testParametersCategory -> switch (testParametersCategory) {
                            case INTERNAL_USER_API_IF_INTERNAL_API_ENABLED,
                                 INTERNAL_ADMIN_API_IF_INTERNAL_API_ENABLED -> new ExpectedResult(HttpStatus.FORBIDDEN);
                            case ACTUATOR -> new ExpectedResult(HttpStatus.OK);
                            default -> new ExpectedResult(HttpStatus.UNAUTHORIZED);
                        }
                ),
                Tuples.of(
                        createPersonalToken(
                                PersonalTokenOrgId.WITH_ALL_USER_ACCESS,
                                PersonalTokenObjectIdentifier.WITH_NO_USER_PERMISSION,
                                Set.of(UserRole.DEVELOPER)
                        ),
                        testParametersCategory -> switch (testParametersCategory) {
                            case INTERNAL_USER_API_IF_INTERNAL_API_ENABLED,
                                 INTERNAL_ADMIN_API_IF_INTERNAL_API_ENABLED -> new ExpectedResult(
                                    HttpStatus.OK,
                                    Set.of("ROLE_USER", "ROLE_ADMIN", "ROLE_DEVELOPER")
                            );
                            case ACTUATOR -> new ExpectedResult(HttpStatus.OK);
                            default -> new ExpectedResult(HttpStatus.UNAUTHORIZED);
                        }
                ),
                Tuples.of(
                        createPersonalToken(
                                PersonalTokenOrgId.WITH_ALL_USER_ACCESS,
                                PersonalTokenObjectIdentifier.WITH_NO_SA_AUTHORIZATIONS,
                                Set.of(UserRole.DEVELOPER)
                        ),
                        testParametersCategory -> switch (testParametersCategory) {
                            case INTERNAL_USER_API_IF_INTERNAL_API_ENABLED,
                                 INTERNAL_ADMIN_API_IF_INTERNAL_API_ENABLED -> new ExpectedResult(
                                    HttpStatus.OK,
                                    Set.of(
                                            "ROLE_USER", "ROLE_ADMIN", "ROLE_DEVELOPER"
                                    )
                            );
                            case ACTUATOR -> new ExpectedResult(HttpStatus.OK);
                            default -> new ExpectedResult(HttpStatus.UNAUTHORIZED);
                        }
                ),
                Tuples.of(
                        createPersonalToken(
                                PersonalTokenOrgId.WITH_ALL_USER_ACCESS,
                                PersonalTokenObjectIdentifier.WITH_SA_1_2_AUTHORIZATIONS,
                                Set.of(UserRole.DEVELOPER)
                        ),
                        testParametersCategory -> switch (testParametersCategory) {
                            case INTERNAL_USER_API_IF_INTERNAL_API_ENABLED,
                                 INTERNAL_ADMIN_API_IF_INTERNAL_API_ENABLED -> new ExpectedResult(
                                    HttpStatus.OK,
                                    Set.of(
                                            "ROLE_USER", "ROLE_ADMIN", "ROLE_DEVELOPER",
                                            "SOURCE_APPLICATION_ID_1", "SOURCE_APPLICATION_ID_2"
                                    )
                            );
                            case ACTUATOR -> new ExpectedResult(HttpStatus.OK);
                            default -> new ExpectedResult(HttpStatus.UNAUTHORIZED);
                        }
                ),
                Tuples.of(
                        createPersonalToken(
                                PersonalTokenOrgId.WITH_ADMIN_AND_DEV_ACCESS,
                                PersonalTokenObjectIdentifier.WITH_SA_1_2_AUTHORIZATIONS,
                                Set.of(UserRole.DEVELOPER)
                        ),
                        testParametersCategory -> switch (testParametersCategory) {
                            case INTERNAL_USER_API_IF_INTERNAL_API_ENABLED,
                                 INTERNAL_ADMIN_API_IF_INTERNAL_API_ENABLED -> new ExpectedResult(
                                    HttpStatus.OK,
                                    Set.of(
                                            "ROLE_USER", "ROLE_ADMIN", "ROLE_DEVELOPER",
                                            "SOURCE_APPLICATION_ID_1", "SOURCE_APPLICATION_ID_2"
                                    )
                            );
                            case ACTUATOR -> new ExpectedResult(HttpStatus.OK);
                            default -> new ExpectedResult(HttpStatus.UNAUTHORIZED);
                        }
                ),

                Tuples.of(
                        createClientToken(ClientId.AUTHORIZED_FOR_INTERNAL_CLIENT_API),
                        testParametersCategory ->
                                testParametersCategory == INTERNAL_CLIENT_API_IF_ENABLED
                                || testParametersCategory == ACTUATOR
                                        ? new ExpectedResult(HttpStatus.OK)
                                        : new ExpectedResult(HttpStatus.UNAUTHORIZED)
                ),
                Tuples.of(
                        createClientToken(ClientId.NOT_AUTHORIZED_FOR_INTERNAL_CLIENT_API),
                        testParametersCategory -> switch (testParametersCategory) {
                            case INTERNAL_CLIENT_API_IF_ENABLED -> new ExpectedResult(HttpStatus.FORBIDDEN);
                            case ACTUATOR -> new ExpectedResult(HttpStatus.OK);
                            default -> new ExpectedResult(HttpStatus.UNAUTHORIZED);
                        }
                ),


                Tuples.of(
                        createClientToken(ClientId.WITH_EXTERNAL_CLIENT_SA_AUTHORIZATION_ID_1),
                        testParametersCategory -> switch (testParametersCategory) {
                            case INTERNAL_CLIENT_API_IF_ENABLED -> new ExpectedResult(HttpStatus.FORBIDDEN);
                            case EXTERNAL_CLIENT_API_IF_ENABLED -> new ExpectedResult(
                                    HttpStatus.OK,
                                    Set.of("SOURCE_APPLICATION_ID_1")
                            );
                            case ACTUATOR -> new ExpectedResult(HttpStatus.OK);
                            default -> new ExpectedResult(HttpStatus.UNAUTHORIZED);
                        }
                ),
                Tuples.of(
                        createClientToken(ClientId.WITH_NO_EXTERNAL_CLIENT_SA_AUTHORIZATION),
                        testParametersCategory -> switch (testParametersCategory) {
                            case INTERNAL_CLIENT_API_IF_ENABLED -> new ExpectedResult(HttpStatus.FORBIDDEN);
                            case ACTUATOR -> new ExpectedResult(HttpStatus.OK);
                            default -> new ExpectedResult(HttpStatus.UNAUTHORIZED);
                        }
                )
        );
    }
}
