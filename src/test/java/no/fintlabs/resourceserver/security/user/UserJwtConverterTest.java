package no.fintlabs.resourceserver.security.user;

import no.fintlabs.cache.FintCache;
import no.fintlabs.resourceserver.security.RoleAuthorityMappingService;
import no.fintlabs.resourceserver.security.SourceApplicationAuthorityMappingService;
import no.fintlabs.resourceserver.security.user.userpermission.UserPermission;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.annotation.DirtiesContext;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DirtiesContext
class UserJwtConverterTest {

    @Mock
    private FintCache<UUID, UserPermission> userPermissionCache;
    @Mock
    private SourceApplicationAuthorityMappingService sourceApplicationAuthorityMappingService;
    @Mock
    private RoleAuthorityMappingService roleAuthorityMappingService;
    @Mock
    private UserRoleFilteringService userRoleFilteringService;
    @InjectMocks
    private UserJwtConverter converter;
    @Mock
    private Jwt jwt;

    @Test
    void givenNoOrganizationIdClaimShouldThrowIllegalArgumentException() {
        when(jwt.getClaimAsString(UserClaim.ORGANIZATION_ID.getJwtTokenClaimName()))
                .thenReturn(null);

        StepVerifier.create(converter.convert(jwt))
                .expectErrorSatisfies(throwable -> {
                    assertThat(throwable).isInstanceOf(IllegalArgumentException.class);
                    assertThat(throwable.getMessage()).isEqualTo("Missing Claim: " + UserClaim.ORGANIZATION_ID);
                })
                .verify();
    }

    @Test
    void givenNoObjectIdentifierClaimShouldThrowIllegalArgumentException() {
        when(jwt.getClaimAsString(UserClaim.ORGANIZATION_ID.getJwtTokenClaimName()))
                .thenReturn("testOrganizationId");

        StepVerifier.create(converter.convert(jwt))
                .expectErrorSatisfies(throwable -> {
                    assertThat(throwable).isInstanceOf(IllegalArgumentException.class);
                    assertThat(throwable.getMessage()).isEqualTo("Missing Claim: " + UserClaim.OBJECT_IDENTIFIER);
                })
                .verify();
    }

    @Test
    void givenValidJwtTokenWithoutUserPermissionAndRoleShouldReturnJwtAuthenticationTokenWithoutAuthorities() {
        when(jwt.getClaimAsString(UserClaim.ORGANIZATION_ID.getJwtTokenClaimName()))
                .thenReturn("testOrganizationId");
        UUID objectIdentifier = UUID.fromString("377cfaae-ef8f-4060-86b6-1cd083bfde07");
        when(jwt.getClaimAsString(UserClaim.OBJECT_IDENTIFIER.getJwtTokenClaimName()))
                .thenReturn(objectIdentifier.toString());

        when(userPermissionCache.getOptional(objectIdentifier))
                .thenReturn(Optional.empty());

        when(jwt.getClaimAsStringList(UserClaim.ROLES.getJwtTokenClaimName())).thenReturn(List.of());

        StepVerifier.create(converter.convert(jwt))
                .assertNext(authentication -> {
                    assertThat(authentication).isInstanceOf(JwtAuthenticationToken.class);
                    assertThat(authentication.getAuthorities()).isEmpty();
                    assertThat(authentication.isAuthenticated()).isTrue();
                })
                .expectComplete()
                .verify();

        verify(jwt).getClaimAsString(UserClaim.ORGANIZATION_ID.getJwtTokenClaimName());
        verify(jwt).getClaimAsString(UserClaim.OBJECT_IDENTIFIER.getJwtTokenClaimName());
        verify(userPermissionCache).getOptional(objectIdentifier);
        verify(jwt).getClaimAsStringList(UserClaim.ROLES.getJwtTokenClaimName());
        verifyNoMoreInteractions(
                roleAuthorityMappingService,
                sourceApplicationAuthorityMappingService,
                userPermissionCache,
                userRoleFilteringService
        );
    }

    @Test
    void givenValidJwtTokenWithUserPermissionAndRoleShouldReturnJwtAuthenticationTokenWithAuthorities() {
        when(jwt.getClaimAsString(UserClaim.ORGANIZATION_ID.getJwtTokenClaimName()))
                .thenReturn("testOrganizationId");
        UUID objectIdentifier = UUID.fromString("377cfaae-ef8f-4060-86b6-1cd083bfde07");
        when(jwt.getClaimAsString(UserClaim.OBJECT_IDENTIFIER.getJwtTokenClaimName()))
                .thenReturn(objectIdentifier.toString());

        UserPermission userPermission = mock(UserPermission.class);
        List<Long> sourceApplicationIds = List.of(1234L);
        when(userPermission.getSourceApplicationIds())
                .thenReturn(sourceApplicationIds);

        GrantedAuthority sourceApplicationAuthority1 = mock(GrantedAuthority.class);
        GrantedAuthority sourceApplicationAuthority2 = mock(GrantedAuthority.class);
        when(sourceApplicationAuthorityMappingService.createSourceApplicationAuthorities(sourceApplicationIds)).thenReturn(List.of(
                sourceApplicationAuthority1, sourceApplicationAuthority2
        ));

        when(userPermissionCache.getOptional(objectIdentifier))
                .thenReturn(Optional.of(userPermission));

        List<String> roleClaims = List.of("TEST_ROLE_1", "TEST_ROLE_2");
        when(jwt.getClaimAsStringList(UserClaim.ROLES.getJwtTokenClaimName())).thenReturn(roleClaims);

        Set<UserRole> filteredUserRoles = Set.of(UserRole.USER);
        when(userRoleFilteringService.filter(roleClaims, "testOrganizationId"))
                .thenReturn(filteredUserRoles);

        GrantedAuthority roleAuthority = mock(GrantedAuthority.class);
        when(roleAuthorityMappingService.createRoleAuthorities(filteredUserRoles))
                .thenReturn(List.of(roleAuthority));

        StepVerifier.create(converter.convert(jwt))
                .assertNext(authentication -> {
                    assertThat(authentication).isInstanceOf(JwtAuthenticationToken.class);
                    assertThat(authentication.getAuthorities()).containsExactlyInAnyOrder(
                            sourceApplicationAuthority1,
                            sourceApplicationAuthority2,
                            roleAuthority
                    );
                    assertThat(authentication.isAuthenticated()).isTrue();
                })
                .expectComplete()
                .verify();

        verify(jwt).getClaimAsString(UserClaim.ORGANIZATION_ID.getJwtTokenClaimName());
        verify(jwt).getClaimAsString(UserClaim.OBJECT_IDENTIFIER.getJwtTokenClaimName());
        verify(userPermission).getSourceApplicationIds();
        verify(sourceApplicationAuthorityMappingService).createSourceApplicationAuthorities(sourceApplicationIds);
        verify(userPermissionCache).getOptional(objectIdentifier);
        verify(jwt).getClaimAsStringList(UserClaim.ROLES.getJwtTokenClaimName());
        verify(userRoleFilteringService).filter(roleClaims, "testOrganizationId");
        verify(roleAuthorityMappingService).createRoleAuthorities(filteredUserRoles);
        verifyNoMoreInteractions(
                userPermission,
                roleAuthorityMappingService,
                sourceApplicationAuthorityMappingService,
                userPermissionCache,
                userRoleFilteringService
        );
    }
}
