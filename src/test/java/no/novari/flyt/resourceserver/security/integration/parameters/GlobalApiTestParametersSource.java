package no.novari.flyt.resourceserver.security.integration.parameters;

import no.novari.flyt.resourceserver.security.integration.utils.TokenWrapper;
import no.novari.flyt.resourceserver.security.integration.utils.testValues.ClientId;
import no.novari.flyt.resourceserver.security.integration.utils.testValues.PersonalTokenObjectIdentifier;
import no.novari.flyt.resourceserver.security.integration.utils.testValues.PersonalTokenOrgId;
import no.novari.flyt.resourceserver.security.user.UserRole;
import org.springframework.http.HttpStatus;

import java.util.Set;
import java.util.stream.Stream;

import static no.novari.flyt.resourceserver.security.integration.utils.TokenFactory.createClientToken;
import static no.novari.flyt.resourceserver.security.integration.utils.TokenFactory.createPersonalToken;

public class GlobalApiTestParametersSource {

    private static final TestParametersFactory FACTORY = new TestParametersFactory("/not/matching/any/filter");

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
                                Set.of(UserRole.ADMIN)
                        ),
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
