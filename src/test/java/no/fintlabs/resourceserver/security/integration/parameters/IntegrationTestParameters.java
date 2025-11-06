package no.fintlabs.resourceserver.security.integration.parameters;

import lombok.Getter;
import no.fintlabs.resourceserver.security.integration.utils.TokenWrapper;
import org.springframework.http.HttpStatus;

import java.util.Set;
import java.util.StringJoiner;

@Getter
public class IntegrationTestParameters {
    String path;
    TokenWrapper tokenWrapper;
    HttpStatus expectedResponseHttpStatus;
    Set<String> expectedAuthorities;

    public IntegrationTestParameters(
            String path,
            TokenWrapper tokenWrapper,
            HttpStatus expectedResponseHttpStatus
    ) {
        this(path, tokenWrapper, expectedResponseHttpStatus, null);
    }

    public IntegrationTestParameters(
            String path,
            TokenWrapper tokenWrapper,
            HttpStatus expectedResponseHttpStatus,
            Set<String> expectedAuthorities
    ) {
        this.path = path;
        this.tokenWrapper = tokenWrapper;
        this.expectedResponseHttpStatus = expectedResponseHttpStatus;
        this.expectedAuthorities = expectedAuthorities;
    }

    @Override
    public String toString() {
        StringJoiner stringJoiner = new StringJoiner(" | ")
                .add(path)
                .add("Token: " + tokenWrapper.getTokenDescription())
                .add("Exp resp: " + expectedResponseHttpStatus);
        if (expectedAuthorities != null) {
            stringJoiner.add("Exp authz: " + expectedAuthorities);
        }
        return stringJoiner.toString();
    }

}
