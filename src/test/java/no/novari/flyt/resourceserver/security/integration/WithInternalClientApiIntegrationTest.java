package no.novari.flyt.resourceserver.security.integration;


import no.novari.flyt.resourceserver.security.integration.parameters.TestParameters;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.MethodSources;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("internal-client-api")
public class WithInternalClientApiIntegrationTest extends AbstractIntegrationTest {

    @ParameterizedTest
    @MethodSources({
            @MethodSource("no.novari.flyt.resourceserver.security.integration.parameters.TestParametersSource#internalApiDisabled"),
            @MethodSource("no.novari.flyt.resourceserver.security.integration.parameters.TestParametersSource#internalClientApiEnabled"),
            @MethodSource("no.novari.flyt.resourceserver.security.integration.parameters.TestParametersSource#externalClientApiDisabled"),
            @MethodSource("no.novari.flyt.resourceserver.security.integration.parameters.TestParametersSource#actuator"),
            @MethodSource("no.novari.flyt.resourceserver.security.integration.parameters.TestParametersSource#global")
    })
    public void test(TestParameters testParameters) {
        super.performIntegrationTest(testParameters);
    }
}
