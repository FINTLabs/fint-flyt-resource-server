package no.novari.flyt.resourceserver.security.client.internal;

import no.novari.flyt.resourceserver.security.AuthorityMappingService;
import no.novari.flyt.resourceserver.security.AuthorityPrefix;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DirtiesContext
public class InternalClientAuthorityMappingServiceTest {

    @Mock
    private AuthorityMappingService authorityMappingService;

    @InjectMocks
    private InternalClientAuthorityMappingService internalClientAuthorityMappingService;

    @Test
    void createInternalClientIdAuthority() {
        String clientId = "testClientIdValue";

        when(authorityMappingService.toAuthority(AuthorityPrefix.CLIENT_ID, clientId))
                .thenReturn("CLIENT_ID_testClientIdValue");

        GrantedAuthority internalClientIdAuthority =
                internalClientAuthorityMappingService.createInternalClientIdAuthority(clientId);

        assertThat(internalClientIdAuthority.getAuthority()).isEqualTo("CLIENT_ID_testClientIdValue");

    }

}
