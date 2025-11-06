package no.fintlabs.resourceserver.security.client.sourceapplication;

import no.fintlabs.resourceserver.security.AuthorityMappingService;
import no.fintlabs.resourceserver.security.AuthorityPrefix;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DirtiesContext
public class SourceApplicationAuthorityMappingServiceTest {

    @Mock
    private AuthorityMappingService authorityMappingService;

    @InjectMocks
    private SourceApplicationAuthorityMappingService sourceApplicationAuthorityMappingService;

    @Test
    void givenSourceApplicationIdShouldReturnGrantedAuthority() {
        Long sourceApplicationId = 2L;

        when(authorityMappingService.toAuthority(AuthorityPrefix.SOURCE_APPLICATION_ID, String.valueOf(sourceApplicationId)))
                .thenReturn("SOURCE_APPLICATION_ID_2");

        GrantedAuthority sourceApplicationAuthority = sourceApplicationAuthorityMappingService.createSourceApplicationAuthority(sourceApplicationId);

        assertThat(sourceApplicationAuthority).isEqualTo(
                new SimpleGrantedAuthority("SOURCE_APPLICATION_ID_2")
        );
    }
}
