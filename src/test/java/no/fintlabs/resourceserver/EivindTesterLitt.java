package no.fintlabs.resourceserver;

import lombok.Builder;
import no.fintlabs.resourceserver.security.properties.ExternalApiSecurityProperties;
import no.fintlabs.resourceserver.security.properties.InternalApiSecurityProperties;
import no.fintlabs.resourceserver.security.properties.InternalClientApiSecurityProperties;
import no.fintlabs.resourceserver.security.user.userpermission.UserPermission;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;


public class EivindTesterLitt {

    // TODO 27/10/2025 eivindmorch: Params:
    //  STATE
    //      – Application properties
    //          – Internal
    //              – Role filter
    //          – Internal client
    //              – Authorized client ids
    //          – External client
    //              – Authorized client ids
    //      – User permissions
    //      – Source application authorization
    //  REQUEST
    //      – Path
    //          – Internal user
    //          – Internal client
    //          – External client
    //      – Token
    //          – None
    //          – User
    //              – No org
    //              – Org, no object identifier
    //              – Org, object identifier, roles
    //          – Client
    //              – No client id
    //              – Client id
    //  EXPECTED OUTCOME
    //      – Forbidden
    //      – Unauthorized
    //      – Authorized
    //          – Authorities
    //              – Roles
    //              – Source application id(s)

    void a() {
        TestParameters
                .builder()
                .internalApiSecurityProperties(InternalApiSecurityProperties
                        .builder()
                        .enabled(true)
                        .userRoleFilterPerOrgId(Map.of(

                        ))
                        .build())
                .internalClientApiSecurityProperties(
                        InternalClientApiSecurityProperties
                                .builder()
                                .enabled(false)
                                .build()
                )
                .externalApiSecurityProperties(
                        ExternalApiSecurityProperties
                                .builder()
                                .enabled(false)
                                .build()
                )
                .userPermissions(
                        Map.of(UUID.fromString("a3be307e-e8d4-4475-8ed0-8d948dc47b86"), UserPermission
                                .builder()
                                .objectIdentifier(UUID.fromString("a3be307e-e8d4-4475-8ed0-8d948dc47b86"))
                                .sourceApplicationIds(List.of(1L, 2L, 3L))
                                .build()
                        )
                )
                .sourceApplicationAuthorizations(Map.of(
                        "clientId", 1L
                ))
                .path("pathing")
                .jwt(Jwt.withTokenValue("asd").build())
                .expectedResponse(ResponseEntity.ok().build())
                .expectedAuthorities(List.of("ROLE_USER"))
                .build();
    }

    @Builder
    private static class TestParameters {
        InternalApiSecurityProperties internalApiSecurityProperties;
        InternalClientApiSecurityProperties internalClientApiSecurityProperties;
        ExternalApiSecurityProperties externalApiSecurityProperties;
        Map<UUID, UserPermission> userPermissions;
        Map<String, Long> sourceApplicationAuthorizations;
        String path;
        Jwt jwt;
        ResponseEntity<String> expectedResponse;
        List<GrantedAuthority> expectedAuthorities;
    }

}
