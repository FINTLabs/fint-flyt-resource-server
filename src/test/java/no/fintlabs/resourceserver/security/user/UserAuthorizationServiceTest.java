package no.fintlabs.resourceserver.security.user;

import no.fintlabs.resourceserver.security.AuthorityMappingService;
import no.fintlabs.resourceserver.security.AuthorityPrefix;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserAuthorizationServiceTest {

    private AuthorityMappingService authorityMappingService;
    private UserAuthorizationService userAuthorizationService;

    @BeforeEach
    public void setup() {
        authorityMappingService = mock(AuthorityMappingService.class);
        userAuthorizationService = new UserAuthorizationService(authorityMappingService);
    }

    @Test
    void givenNoUserAuthorizedSourceApplicationIdsWhenCheckIfUserHasAccessShouldReturnForbidden() {
        Authentication authentication = mock(Authentication.class);
        Collection<? extends GrantedAuthority> authorities = mock(Collection.class);
        Mockito.<Collection<? extends GrantedAuthority>>when(authentication.getAuthorities())
                .thenReturn(authorities);

        when(authorityMappingService.extractLongValues(
                AuthorityPrefix.SOURCE_APPLICATION_ID,
                authorities
        )).thenReturn(Set.of());

        ResponseStatusException responseStatusException = assertThrows(
                ResponseStatusException.class,
                () -> userAuthorizationService.checkIfUserHasAccessToSourceApplication(
                        authentication,
                        1L
                )
        );

        assertThat(responseStatusException.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        verify(authorityMappingService).extractLongValues(
                AuthorityPrefix.SOURCE_APPLICATION_ID,
                authorities
        );
        verifyNoMoreInteractions(authorityMappingService);
    }

    @Test
    void givenUserAuthorizedSourceApplicationIdsNotMatchingCheckedIdWhenCheckIfUserHasAccessShouldReturnForbidden() {
        Authentication authentication = mock(Authentication.class);
        Collection<? extends GrantedAuthority> authorities = mock(Collection.class);
        Mockito.<Collection<? extends GrantedAuthority>>when(authentication.getAuthorities())
                .thenReturn(authorities);

        when(authorityMappingService.extractLongValues(
                AuthorityPrefix.SOURCE_APPLICATION_ID,
                authorities
        )).thenReturn(Set.of(2L, 3L));

        ResponseStatusException responseStatusException = assertThrows(
                ResponseStatusException.class,
                () -> userAuthorizationService.checkIfUserHasAccessToSourceApplication(
                        authentication,
                        1L
                )
        );

        assertThat(responseStatusException.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        verify(authorityMappingService).extractLongValues(
                AuthorityPrefix.SOURCE_APPLICATION_ID,
                authorities
        );
        verifyNoMoreInteractions(authorityMappingService);
    }

    @Test
    void givenUserAuthorizedSourceApplicationIdMatchingCheckedIdWhenCheckIfUserHasAccessShouldReturnSuccessfully() {
        Authentication authentication = mock(Authentication.class);
        Collection<? extends GrantedAuthority> authorities = mock(Collection.class);
        Mockito.<Collection<? extends GrantedAuthority>>when(authentication.getAuthorities())
                .thenReturn(authorities);

        when(authorityMappingService.extractLongValues(
                AuthorityPrefix.SOURCE_APPLICATION_ID,
                authorities
        )).thenReturn(Set.of(2L, 3L));

        assertDoesNotThrow(
                () -> userAuthorizationService.checkIfUserHasAccessToSourceApplication(
                        authentication,
                        3L
                )
        );

        verify(authorityMappingService).extractLongValues(
                AuthorityPrefix.SOURCE_APPLICATION_ID,
                authorities
        );
        verifyNoMoreInteractions(authorityMappingService);
    }

    @Test
    void givenNoUserRoleAuthoritiesWhenUserHasRoleCalledShouldReturnFalse() {
        Authentication authentication = mock(Authentication.class);
        Collection<? extends GrantedAuthority> authorities = mock(Collection.class);
        Mockito.<Collection<? extends GrantedAuthority>>when(authentication.getAuthorities())
                .thenReturn(authorities);

        when(authorityMappingService.extractStringValues(
                AuthorityPrefix.ROLE,
                authorities
        )).thenReturn(Set.of());

        assertThat(
                userAuthorizationService.userHasRole(
                        authentication,
                        UserRole.USER
                )
        ).isFalse();

        verify(authorityMappingService).extractStringValues(
                AuthorityPrefix.ROLE,
                authorities
        );
        verifyNoMoreInteractions(authorityMappingService);
    }

    @Test
    void givenUserRoleAuthorityNotMatchingCheckedUserRoleWhenUserHasRoleCalledShouldReturnFalse() {
        Authentication authentication = mock(Authentication.class);
        Collection<? extends GrantedAuthority> authorities = mock(Collection.class);
        Mockito.<Collection<? extends GrantedAuthority>>when(authentication.getAuthorities())
                .thenReturn(authorities);

        when(authorityMappingService.extractStringValues(
                AuthorityPrefix.ROLE,
                authorities
        )).thenReturn(Set.of(
                "roleAuthorityValue1",
                "roleAuthorityValue2"
        ));

        assertThat(
                userAuthorizationService.userHasRole(
                        authentication,
                        UserRole.DEVELOPER
                )
        ).isFalse();

        verify(authorityMappingService).extractStringValues(
                AuthorityPrefix.ROLE,
                authorities
        );
        verifyNoMoreInteractions(authorityMappingService);
    }

    @Test
    void givenUserRoleAuthorityMatchingCheckedUserRoleWhenUserHasRoleCalledShouldReturnTrue() {
        Authentication authentication = mock(Authentication.class);
        Collection<? extends GrantedAuthority> authorities = mock(Collection.class);
        Mockito.<Collection<? extends GrantedAuthority>>when(authentication.getAuthorities())
                .thenReturn(authorities);

        when(authorityMappingService.extractStringValues(
                AuthorityPrefix.ROLE,
                authorities
        )).thenReturn(Set.of(
                "roleAuthorityValue1",
                UserRole.DEVELOPER.name()
        ));

        assertThat(
                userAuthorizationService.userHasRole(
                        authentication,
                        UserRole.DEVELOPER
                )
        ).isTrue();

        verify(authorityMappingService).extractStringValues(
                AuthorityPrefix.ROLE,
                authorities
        );
        verifyNoMoreInteractions(authorityMappingService);
    }

}
