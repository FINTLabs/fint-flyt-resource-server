package no.novari.flyt.resourceserver.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthorityPrefix {
    ORG_ID("ORG_ID"),
    ROLE("ROLE"),
    SOURCE_APPLICATION_ID("SOURCE_APPLICATION_ID"),
    CLIENT_ID("CLIENT_ID");

    private final String value;

}
