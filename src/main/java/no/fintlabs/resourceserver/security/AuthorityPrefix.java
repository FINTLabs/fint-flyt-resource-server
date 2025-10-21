package no.fintlabs.resourceserver.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthorityPrefix {
    ORG_ID("ORG_ID"),
    ROLE("ROLE"),
    SOURCE_APPLICATION_ID("SOURCE_APPLICATION_ID"), // TODO 21/10/2025 eivindmorch: Separate source app client use and user access use?
    CLIENT_ID("CLIENT_ID");

    private final String value;

}
