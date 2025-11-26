package no.novari.flyt.resourceserver.security.client.sourceapplication;

import no.novari.flyt.resourceserver.security.AuthorityMappingService;
import no.novari.flyt.resourceserver.security.AuthorityPrefix;
import no.novari.flyt.resourceserver.security.client.sourceapplication.exceptions.MultipleSourceApplicationIdsException;
import no.novari.flyt.resourceserver.security.client.sourceapplication.exceptions.NoSourceApplicationIdException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class SourceApplicationAuthorizationServiceTest {

    private AuthorityMappingService authorityMappingService;
    private SourceApplicationAuthorizationService sourceApplicationAuthorizationService;

    @BeforeEach
    public void setup() {
        authorityMappingService = mock(AuthorityMappingService.class);
        sourceApplicationAuthorizationService = new SourceApplicationAuthorizationService(authorityMappingService);
    }

    @Test
    void givenSingleSourceApplicationIdAuthorityShouldReturnSourceApplicationId() {
        Authentication authentication = mock(Authentication.class);
        Collection<? extends GrantedAuthority> authorities = mock(Collection.class);
        Mockito.<Collection<? extends GrantedAuthority>>when(authentication.getAuthorities())
                .thenReturn(authorities);

        when(authorityMappingService.extractLongValues(
                AuthorityPrefix.SOURCE_APPLICATION_ID,
                authorities
        )).thenReturn(Set.of(1L));

        assertThat(sourceApplicationAuthorizationService.getSourceApplicationId(authentication))
                .isEqualTo(1L);

        verify(authentication).getAuthorities();
        verify(authorityMappingService).extractLongValues(
                AuthorityPrefix.SOURCE_APPLICATION_ID,
                authorities
        );
        verifyNoMoreInteractions(authentication, authorityMappingService);
    }

    @Test
    void givenNoSourceApplicationIdAuthoritiesShouldThrowException() {
        Authentication authentication = mock(Authentication.class);
        Collection<? extends GrantedAuthority> authorities = mock(Collection.class);
        Mockito.<Collection<? extends GrantedAuthority>>when(authentication.getAuthorities())
                .thenReturn(authorities);

        when(authorityMappingService.extractLongValues(
                AuthorityPrefix.SOURCE_APPLICATION_ID,
                authorities
        )).thenReturn(Set.of());

        assertThrows(
                NoSourceApplicationIdException.class,
                () -> sourceApplicationAuthorizationService.getSourceApplicationId(authentication)
        );

        verify(authentication).getAuthorities();
        verify(authorityMappingService).extractLongValues(
                AuthorityPrefix.SOURCE_APPLICATION_ID,
                authorities
        );
        verifyNoMoreInteractions(authentication, authorityMappingService);
    }

    @Test
    void givenMultipleSourceApplicationIdAuthoritiesShouldThrowException() {
        Authentication authentication = mock(Authentication.class);
        Collection<? extends GrantedAuthority> authorities = mock(Collection.class);
        Mockito.<Collection<? extends GrantedAuthority>>when(authentication.getAuthorities())
                .thenReturn(authorities);

        when(authorityMappingService.extractLongValues(
                AuthorityPrefix.SOURCE_APPLICATION_ID,
                authorities
        )).thenReturn(Set.of(1L, 2L));

        assertThrows(
                MultipleSourceApplicationIdsException.class,
                () -> sourceApplicationAuthorizationService.getSourceApplicationId(authentication)
        );

        verify(authentication).getAuthorities();
        verify(authorityMappingService).extractLongValues(
                AuthorityPrefix.SOURCE_APPLICATION_ID,
                authorities
        );
        verifyNoMoreInteractions(authentication, authorityMappingService);
    }

}