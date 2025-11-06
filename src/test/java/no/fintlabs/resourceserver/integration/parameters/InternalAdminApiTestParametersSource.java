package no.fintlabs.resourceserver.integration.parameters;

import no.fintlabs.resourceserver.UrlPaths;
import no.fintlabs.resourceserver.integration.utils.TokenWrapper;
import no.fintlabs.resourceserver.integration.utils.testValues.ClientId;
import no.fintlabs.resourceserver.integration.utils.testValues.PersonalTokenObjectIdentifier;
import no.fintlabs.resourceserver.integration.utils.testValues.PersonalTokenOrgId;
import no.fintlabs.resourceserver.security.user.UserRole;
import org.springframework.http.HttpStatus;

import java.util.Set;
import java.util.stream.Stream;

import static no.fintlabs.resourceserver.integration.utils.TokenFactory.createClientToken;
import static no.fintlabs.resourceserver.integration.utils.TokenFactory.createPersonalToken;

public class InternalAdminApiTestParametersSource {

    private static final TestParametersFactory FACTORY = new TestParametersFactory(UrlPaths.INTERNAL_ADMIN_API + "/dummy");

    public static Stream<IntegrationTestParameters> generate() {
        return Stream.of(
                FACTORY.createParameters(
                        TokenWrapper.none(),
                        HttpStatus.UNAUTHORIZED
                ),
                FACTORY.createParameters(
                        createPersonalToken(
                                PersonalTokenOrgId.WITH_NO_ACCESS,
                                PersonalTokenObjectIdentifier.WITH_NO_USER_PERMISSION,
                                Set.of(UserRole.ADMIN)
                        ),
                        HttpStatus.FORBIDDEN
                ),
                FACTORY.createParameters(
                        createPersonalToken(
                                PersonalTokenOrgId.WITH_ALL_USER_ACCESS,
                                PersonalTokenObjectIdentifier.WITH_NO_USER_PERMISSION,
                                Set.of(UserRole.USER)
                        ),
                        HttpStatus.FORBIDDEN
                ),
                FACTORY.createParameters(
                        createPersonalToken(
                                PersonalTokenOrgId.WITH_ALL_USER_ACCESS,
                                PersonalTokenObjectIdentifier.WITH_SA_1_2_AUTHORIZATIONS,
                                Set.of(UserRole.USER)
                        ),
                        HttpStatus.FORBIDDEN
                ),
                FACTORY.createParameters(
                        createPersonalToken(
                                PersonalTokenOrgId.WITH_ALL_USER_ACCESS,
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
                FACTORY.createParameters(
                        createPersonalToken(
                                PersonalTokenOrgId.WITH_ALL_USER_ACCESS,
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
                FACTORY.createParameters(
                        createPersonalToken(
                                PersonalTokenOrgId.WITH_ADMIN_AND_DEV_ACCESS,
                                PersonalTokenObjectIdentifier.WITH_NO_SA_AUTHORIZATIONS,
                                Set.of(UserRole.USER)
                        ),
                        HttpStatus.FORBIDDEN
                ),
                FACTORY.createParameters(
                        createPersonalToken(
                                PersonalTokenOrgId.WITH_ADMIN_AND_DEV_ACCESS,
                                PersonalTokenObjectIdentifier.WITH_NO_SA_AUTHORIZATIONS,
                                Set.of(UserRole.ADMIN)
                        ),
                        HttpStatus.OK,
                        Set.of(
                                "ROLE_USER",
                                "ROLE_ADMIN"
                        )
                ),
                FACTORY.createParameters(
                        createPersonalToken(
                                PersonalTokenOrgId.WITH_ADMIN_AND_DEV_ACCESS,
                                PersonalTokenObjectIdentifier.WITH_NO_SA_AUTHORIZATIONS,
                                Set.of(UserRole.DEVELOPER)
                        ),
                        HttpStatus.OK,
                        Set.of(
                                "ROLE_USER",
                                "ROLE_ADMIN",
                                "ROLE_DEVELOPER"
                        )
                ),
                FACTORY.createParameters(
                        createClientToken(ClientId.NOT_AUTHORIZED_FOR_INTERNAL_CLIENT_API),
                        HttpStatus.UNAUTHORIZED
                ),
                FACTORY.createParameters(
                        createClientToken(ClientId.AUTHORIZED_FOR_INTERNAL_CLIENT_API),
                        HttpStatus.UNAUTHORIZED
                ),
                FACTORY.createParameters(
                        createClientToken(ClientId.WITH_EXTERNAL_CLIENT_SA_AUTHORIZATION_ID_1),
                        HttpStatus.UNAUTHORIZED
                )
        );
    }

}
