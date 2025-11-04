package no.fintlabs.resourceserver.security.client.sourceapplication;

import no.fintlabs.resourceserver.security.SourceApplicationAuthorityMappingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.test.StepVerifier;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SourceApplicationJwtConverterTest {

    private SourceApplicationAuthorizationRequestService sourceApplicationAuthorizationRequestService;
    private SourceApplicationAuthorityMappingService sourceApplicationAuthorityMappingService;
    private SourceApplicationJwtConverter sourceApplicationJwtConverter;

    @BeforeEach
    void setUp() {
        sourceApplicationAuthorizationRequestService = mock(SourceApplicationAuthorizationRequestService.class);
        sourceApplicationAuthorityMappingService = mock(SourceApplicationAuthorityMappingService.class);
        sourceApplicationJwtConverter = new SourceApplicationJwtConverter(
                sourceApplicationAuthorizationRequestService,
                sourceApplicationAuthorityMappingService
        );
    }

    @Test
    void givenTokenWithNoSubShouldReturnAuthenticationWithNoSourceApplicationIdAuthority() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(null);

        StepVerifier.create(sourceApplicationJwtConverter.convert(jwt))
                .assertNext(authentication ->
                        assertThat(authentication.getAuthorities()).isEmpty())
                .expectComplete()
                .verify();

        verifyNoInteractions(
                sourceApplicationAuthorizationRequestService,
                sourceApplicationAuthorityMappingService
        );
    }

    @Test
    void givenTokenWithSubAndNoClientAuthorizationFromAuthorizationRequestShouldReturnAuthenticationWithNoSourceApplicationIdAuthority() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn("subjectValue");

        when(sourceApplicationAuthorizationRequestService.getClientAuthorization("subjectValue"))
                .thenReturn(Optional.empty());

        StepVerifier.create(sourceApplicationJwtConverter.convert(jwt))
                .assertNext(authentication ->
                        assertThat(authentication.getAuthorities()).isEmpty())
                .expectComplete()
                .verify();

        verify(sourceApplicationAuthorizationRequestService).getClientAuthorization("subjectValue");
        verifyNoMoreInteractions(
                sourceApplicationAuthorizationRequestService,
                sourceApplicationAuthorityMappingService
        );
    }

    @Test
    void givenTokenWithSubAndClientAuthorizationFromAuthorizationRequestShouldCallAuthorityMapperAndReturnAuthenticationWithMappingResult() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn("subjectValue");


        SourceApplicationAuthorization sourceApplicationAuthorization = mock(SourceApplicationAuthorization.class);
        when(sourceApplicationAuthorization.getSourceApplicationId()).thenReturn(3L);
        when(sourceApplicationAuthorizationRequestService.getClientAuthorization("subjectValue"))
                .thenReturn(Optional.of(sourceApplicationAuthorization));

        GrantedAuthority grantedAuthority = mock(GrantedAuthority.class);
        when(sourceApplicationAuthorityMappingService.createSourceApplicationAuthority(3L))
                .thenReturn(grantedAuthority);

        StepVerifier.create(sourceApplicationJwtConverter.convert(jwt))
                .assertNext(authentication ->
                        assertThat(authentication.getAuthorities()).containsExactly(grantedAuthority))
                .expectComplete()
                .verify();

        verify(sourceApplicationAuthorizationRequestService).getClientAuthorization("subjectValue");
        verify(sourceApplicationAuthorityMappingService).createSourceApplicationAuthority(3L);
        verifyNoMoreInteractions(
                sourceApplicationAuthorizationRequestService,
                sourceApplicationAuthorityMappingService
        );
    }


}
