package no.novari.flyt.resourceserver.security.integration.parameters;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Set;
import java.util.StringJoiner;

@Getter
public class ExpectedResult {
    HttpStatus status;
    Set<String> authorities;

    public ExpectedResult(HttpStatus status) {
        this(status, null);
    }

    public ExpectedResult(HttpStatus status, Set<String> authorities) {
        this.status = status;
        this.authorities = authorities;
    }

    @Override
    public String toString() {
        StringJoiner stringJoiner = new StringJoiner(", ", "{", "}");
        stringJoiner.add(status.toString());
        if (authorities != null) {
            stringJoiner.add("Authz: " + authorities);
        }
        return stringJoiner.toString();
    }
}
