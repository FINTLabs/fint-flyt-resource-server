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

public class InternalClientApiTestParametersSource {

    private static final TestParametersFactory FACTORY =
            new TestParametersFactory(UrlPaths.INTERNAL_CLIENT_API + "/dummy");

    public static Stream<IntegrationTestParameters> generate() {
        return Stream.of(
                FACTORY.createParameters(
                        TokenWrapper.none(),
                        HttpStatus.UNAUTHORIZED
                ),
                FACTORY.createParameters(
                        createClientToken(ClientId.NOT_AUTHORIZED_FOR_INTERNAL_CLIENT_API),
                        HttpStatus.FORBIDDEN
                ),
                FACTORY.createParameters(
                        createClientToken(ClientId.AUTHORIZED_FOR_INTERNAL_CLIENT_API),
                        HttpStatus.OK
                ),
                FACTORY.createParameters(
                        createPersonalToken(
                                PersonalTokenOrgId.WITH_ALL_USER_ACCESS,
                                PersonalTokenObjectIdentifier.WITH_SA_1_2_AUTHORIZATIONS,
                                Set.of(UserRole.USER)
                        ),
                        HttpStatus.UNAUTHORIZED
                )
        );

    }
}
