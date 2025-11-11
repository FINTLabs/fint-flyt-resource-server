package no.novari.flyt.resourceserver.security.user;

import no.novari.cache.FintCache;
import no.novari.flyt.resourceserver.security.client.sourceapplication.SourceApplicationAuthorityMappingService;
import no.novari.flyt.resourceserver.security.user.permission.UserPermission;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DirtiesContext
class UserJwtConverterTest {

    @Mock
    private FintCache<UUID, UserPermission> userPermissionCache;
    @Mock
    private SourceApplicationAuthorityMappingService sourceApplicationAuthorityMappingService;
    @Mock
    private UserRoleAuthorityMappingService userRoleAuthorityMappingService;
    @Mock
    private UserRoleFilteringService userRoleFilteringService;
    @Mock
    private UserRoleHierarchyService userRoleHierarchyService;
    @InjectMocks
    private UserJwtConverter converter;
    @Mock
    private Jwt jwt;

    @Test
    void givenNoOrganizationIdClaimShouldReturnJwtAuthenticationTokenWithAuthenticatedFalse() {
        when(jwt.getClaimAsString(UserClaim.ORGANIZATION_ID.getTokenClaimName()))
                .thenReturn(null);

        StepVerifier.create(converter.convert(jwt))
                .assertNext(authentication -> {
                    assertThat(authentication).isInstanceOf(JwtAuthenticationToken.class);
                    assertThat(authentication.getAuthorities()).isEmpty();
                    assertThat(authentication.isAuthenticated()).isFalse();
                })
                .expectComplete()
                .verify();
    }

    @Test
    void givenNoObjectIdentifierClaimShouldReturnJwtAuthenticationTokenWithAuthenticatedFalse() {
        when(jwt.getClaimAsString(UserClaim.ORGANIZATION_ID.getTokenClaimName()))
                .thenReturn("testOrganizationId");

        StepVerifier.create(converter.convert(jwt))
                .assertNext(authentication -> {
                    assertThat(authentication).isInstanceOf(JwtAuthenticationToken.class);
                    assertThat(authentication.getAuthorities()).isEmpty();
                    assertThat(authentication.isAuthenticated()).isFalse();
                })
                .expectComplete()
                .verify();
    }

    @Test
    void givenValidJwtTokenWithoutUserPermissionAndRoleShouldReturnJwtAuthenticationTokenWithoutAuthorities() {
        when(jwt.getClaimAsString(UserClaim.ORGANIZATION_ID.getTokenClaimName()))
                .thenReturn("testOrganizationId");
        UUID objectIdentifier = UUID.fromString("377cfaae-ef8f-4060-86b6-1cd083bfde07");
        when(jwt.getClaimAsString(UserClaim.OBJECT_IDENTIFIER.getTokenClaimName()))
                .thenReturn(objectIdentifier.toString());

        when(userPermissionCache.getOptional(objectIdentifier))
                .thenReturn(Optional.empty());

        when(jwt.getClaimAsStringList(UserClaim.ROLES.getTokenClaimName())).thenReturn(List.of());

        StepVerifier.create(converter.convert(jwt))
                .assertNext(authentication -> {
                    assertThat(authentication).isInstanceOf(JwtAuthenticationToken.class);
                    assertThat(authentication.getAuthorities()).isEmpty();
                    assertThat(authentication.isAuthenticated()).isTrue();
                })
                .expectComplete()
                .verify();

        verify(jwt).getClaimAsString(UserClaim.ORGANIZATION_ID.getTokenClaimName());
        verify(jwt).getClaimAsString(UserClaim.OBJECT_IDENTIFIER.getTokenClaimName());
        verify(userPermissionCache).getOptional(objectIdentifier);
        verify(jwt).getClaimAsStringList(UserClaim.ROLES.getTokenClaimName());
        verifyNoMoreInteractions(
                userRoleAuthorityMappingService,
                sourceApplicationAuthorityMappingService,
                userPermissionCache,
                userRoleFilteringService
        );
    }

    @Test
    void givenValidJwtTokenWithUserPermissionAndRoleShouldReturnJwtAuthenticationTokenWithAuthorities() {
        when(jwt.getClaimAsString(UserClaim.ORGANIZATION_ID.getTokenClaimName()))
                .thenReturn("testOrganizationId");
        UUID objectIdentifier = UUID.fromString("377cfaae-ef8f-4060-86b6-1cd083bfde07");
        when(jwt.getClaimAsString(UserClaim.OBJECT_IDENTIFIER.getTokenClaimName()))
                .thenReturn(objectIdentifier.toString());

        UserPermission userPermission = mock(UserPermission.class);
        Set<Long> sourceApplicationIds = Set.of(1234L);
        when(userPermission.getSourceApplicationIds())
                .thenReturn(sourceApplicationIds);

        GrantedAuthority sourceApplicationAuthority1 = mock(GrantedAuthority.class);
        GrantedAuthority sourceApplicationAuthority2 = mock(GrantedAuthority.class);
        when(sourceApplicationAuthorityMappingService.createSourceApplicationAuthorities(sourceApplicationIds))
                .thenReturn(Set.of(sourceApplicationAuthority1, sourceApplicationAuthority2));

        when(userPermissionCache.getOptional(objectIdentifier))
                .thenReturn(Optional.of(userPermission));

        Set<String> roleClaims = Set.of("TEST_ROLE_1", "TEST_ROLE_2");
        when(jwt.getClaimAsStringList(UserClaim.ROLES.getTokenClaimName())).thenReturn(roleClaims.stream().toList());

        when(userRoleFilteringService.filter(roleClaims, "testOrganizationId"))
                .thenReturn(Set.of(UserRole.ADMIN));

        when(userRoleHierarchyService.getProvidedAndImpliedRoles(Set.of(UserRole.ADMIN)))
                .thenReturn(Set.of(UserRole.ADMIN, UserRole.USER));

        GrantedAuthority roleAuthority = mock(GrantedAuthority.class);
        when(userRoleAuthorityMappingService.createRoleAuthorities(Set.of(UserRole.ADMIN, UserRole.USER)))
                .thenReturn(Set.of(roleAuthority));

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

        verify(jwt).getClaimAsString(UserClaim.ORGANIZATION_ID.getTokenClaimName());
        verify(jwt).getClaimAsString(UserClaim.OBJECT_IDENTIFIER.getTokenClaimName());
        verify(userPermission).getSourceApplicationIds();
        verify(sourceApplicationAuthorityMappingService).createSourceApplicationAuthorities(sourceApplicationIds);
        verify(userPermissionCache).getOptional(objectIdentifier);
        verify(jwt).getClaimAsStringList(UserClaim.ROLES.getTokenClaimName());
        verify(userRoleFilteringService).filter(roleClaims, "testOrganizationId");
        verify(userRoleHierarchyService).getProvidedAndImpliedRoles(Set.of(UserRole.ADMIN));
        verify(userRoleAuthorityMappingService).createRoleAuthorities(Set.of(UserRole.ADMIN, UserRole.USER));
        verifyNoMoreInteractions(
                userPermission,
                userRoleAuthorityMappingService,
                sourceApplicationAuthorityMappingService,
                userPermissionCache,
                userRoleFilteringService,
                userRoleHierarchyService
        );
    }
}
