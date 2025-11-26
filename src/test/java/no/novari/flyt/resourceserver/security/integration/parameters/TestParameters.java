package no.novari.flyt.resourceserver.security.integration.parameters;

import lombok.Getter;

import java.util.StringJoiner;

@Getter
public class TestParameters {
    String path;
    TokenWrapper tokenWrapper;
    ExpectedResult expectedResult;

    public TestParameters(
            String path,
            TokenWrapper tokenWrapper,
            ExpectedResult expectedResult
    ) {
        this.path = path;
        this.tokenWrapper = tokenWrapper;
        this.expectedResult = expectedResult;
    }

    @Override
    public String toString() {
        StringJoiner stringJoiner = new StringJoiner(" | ")
                .add(path)
                .add("Token: " + tokenWrapper.getTokenDescription())
                .add("Exp result: " + expectedResult.toString());
        return stringJoiner.toString();
    }

}
