package no.novari.flyt.resourceserver.security.integration.utils.testValues;

import lombok.AllArgsConstructor;
import lombok.Getter;

// TODO 05/11/2025 eivindmorch: Fix values
@AllArgsConstructor
@Getter
public enum ClientId {
    WITH_EXTERNAL_CLIENT_SA_AUTHORIZATION_ID_1("testClientIdWithExternalClientSaAuthorizationId1"),
    WITH_NO_EXTERNAL_CLIENT_SA_AUTHORIZATION("testClientIdWithNoExternalClientSaAuthorization"),
    AUTHORIZED_FOR_INTERNAL_CLIENT_API("9e8118f3-9bc0-4f00-8675-c04bf8fe2494"),
    NOT_AUTHORIZED_FOR_INTERNAL_CLIENT_API("3d416475-0b4b-473a-b5a2-7742f5e68391");
    private final String claimValue;
}
