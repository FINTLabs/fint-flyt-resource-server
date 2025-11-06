package no.fintlabs.resourceserver.security.integration.parameters;

import lombok.AllArgsConstructor;
import no.fintlabs.resourceserver.security.integration.utils.TokenWrapper;
import org.springframework.http.HttpStatus;

import java.util.Set;

@AllArgsConstructor
public class TestParametersFactory {

    private final String path;

    public IntegrationTestParameters createParameters(
            TokenWrapper tokenWrapper,
            HttpStatus expectedResponseHttpStatus
    ) {
        return new IntegrationTestParameters(
                path,
                tokenWrapper,
                expectedResponseHttpStatus
        );
    }

    public IntegrationTestParameters createParameters(
            TokenWrapper tokenWrapper,
            HttpStatus expectedResponseHttpStatus,
            Set<String> expectedAuthorities
    ) {
        return new IntegrationTestParameters(
                path,
                tokenWrapper,
                expectedResponseHttpStatus,
                expectedAuthorities
        );
    }

}
