package no.fintlabs.resourceserver.security;

import no.fintlabs.resourceserver.security.user.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DirtiesContext
public class RoleAuthorityMappingServiceTest {

    @Mock
    private AuthorityMappingService authorityMappingService;

    @InjectMocks
    private RoleAuthorityMappingService roleAuthorityMappingService;

    @Test
    void createRoleAuthorities() {
        Collection<UserRole> roles = List.of(UserRole.USER, UserRole.DEVELOPER);

        when(authorityMappingService.toAuthority(AuthorityPrefix.ROLE, UserRole.USER.name())).thenReturn("ROLE_USER");
        when(authorityMappingService.toAuthority(AuthorityPrefix.ROLE, UserRole.DEVELOPER.name())).thenReturn("ROLE_DEVELOPER");

        List<GrantedAuthority> roleAuthorities = roleAuthorityMappingService.createRoleAuthorities(roles);

        assertThat(roleAuthorities)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_DEVELOPER");
    }

    @Test
    void createRoleAuthority() {
        UserRole role = UserRole.USER;

        when(authorityMappingService.toAuthority(AuthorityPrefix.ROLE, UserRole.USER.name())).thenReturn("ROLE_USER");

        GrantedAuthority roleAuthority = roleAuthorityMappingService.createRoleAuthority(role);

        assertThat(roleAuthority.getAuthority()).isEqualTo("ROLE_USER");
    }

}
