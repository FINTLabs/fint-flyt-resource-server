package no.fintlabs.resourceserver.security.integration.parameters;

import no.fintlabs.resourceserver.UrlPaths;
import no.fintlabs.resourceserver.security.integration.utils.TokenWrapper;
import no.fintlabs.resourceserver.security.integration.utils.testValues.ClientId;
import no.fintlabs.resourceserver.security.integration.utils.testValues.PersonalTokenObjectIdentifier;
import no.fintlabs.resourceserver.security.integration.utils.testValues.PersonalTokenOrgId;
import no.fintlabs.resourceserver.security.user.UserRole;
import org.springframework.http.HttpStatus;

import java.util.Set;
import java.util.stream.Stream;

import static no.fintlabs.resourceserver.security.integration.utils.TokenFactory.createClientToken;
import static no.fintlabs.resourceserver.security.integration.utils.TokenFactory.createPersonalToken;

public class ExternalClientApiTestParametersSource {

    private static final TestParametersFactory FACTORY = new TestParametersFactory(UrlPaths.EXTERNAL_API + "/dummy");

    public static Stream<IntegrationTestParameters> generate() {
        return Stream.of(
                FACTORY.createParameters(
                        TokenWrapper.none(),
                        HttpStatus.UNAUTHORIZED
                ),
                FACTORY.createParameters(
                        createPersonalToken(
                                PersonalTokenOrgId.WITH_ALL_USER_ACCESS,
                                PersonalTokenObjectIdentifier.WITH_SA_1_2_AUTHORIZATIONS,
                                Set.of(UserRole.USER)
                        ),
                        HttpStatus.UNAUTHORIZED
                ),
                FACTORY.createParameters(
                        createPersonalToken(
                                PersonalTokenOrgId.WITH_ALL_USER_ACCESS,
                                PersonalTokenObjectIdentifier.WITH_SA_1_2_AUTHORIZATIONS,
                                Set.of(UserRole.ADMIN)
                        ),
                        HttpStatus.UNAUTHORIZED
                ),
                FACTORY.createParameters(
                        createClientToken(ClientId.AUTHORIZED_FOR_INTERNAL_CLIENT_API),
                        HttpStatus.UNAUTHORIZED
                )
        );
    }

}
