package no.fintlabs.resourceserver.security;

import no.fintlabs.resourceserver.security.user.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static no.fintlabs.resourceserver.security.AuthorityMappingService.AUTHORITY_DELIMITER;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DirtiesContext
public class AuthorityMappingServiceTest {

    @InjectMocks
    private AuthorityMappingService authorityMappingService;

    @Test
    void givenAuthorityPrefixAndStringValueShouldReturnStringOfAuthorityPrefix() {
        AuthorityPrefix prefix = AuthorityPrefix.CLIENT_ID;
        String value = "something";
        String authorityString = authorityMappingService.toAuthority(prefix, value);

        assertThat(authorityString).isEqualTo("CLIENT_ID_something");
    }

    @Test
    void givenAuthorityPrefixAndCollectionOfGrantedAuthoritiesShouldReturnSetOfLongAuthorityValues() {
        AuthorityPrefix prefix1 = AuthorityPrefix.CLIENT_ID;
        AuthorityPrefix prefix2 = AuthorityPrefix.ROLE;

        Collection<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(prefix1.getValue() + AUTHORITY_DELIMITER + 1L),
                new SimpleGrantedAuthority(prefix1.getValue() + AUTHORITY_DELIMITER + 2L),
                new SimpleGrantedAuthority(prefix2.getValue() + AUTHORITY_DELIMITER + 3L)
        );
        Set<Long> longs = authorityMappingService.extractLongValues(prefix1, authorities);

        assertThat(longs).isEqualTo(
                Set.of(1L, 2L)
        );
    }

    @Test
    void givenAuthorityPrefixAndCollectionOfGrantedAuthoritiesShouldReturnSetOfStringAuthorityValues() {
        AuthorityPrefix prefix1 = AuthorityPrefix.CLIENT_ID;
        AuthorityPrefix prefix2 = AuthorityPrefix.ROLE;

        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(prefix1.getValue() + AUTHORITY_DELIMITER + "a"),
                new SimpleGrantedAuthority(prefix1.getValue() + AUTHORITY_DELIMITER + "b"),
                new SimpleGrantedAuthority(prefix2.getValue() + AUTHORITY_DELIMITER + "c")
        );
        Set<String> strings = authorityMappingService.extractStringValues(AuthorityPrefix.CLIENT_ID, authorities);

        assertThat(strings).isEqualTo(
                Set.of("a", "b")
        );
    }

    @Test
    void createRoleAuthorities() {
        Collection<UserRole> roles = List.of(UserRole.USER, UserRole.DEVELOPER);
        List<GrantedAuthority> roleAuthorities = authorityMappingService.createRoleAuthorities(roles);

        assertThat(roleAuthorities)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_DEVELOPER");
    }

    @Test
    void createRoleAuthority() {
        UserRole role = UserRole.USER;

        GrantedAuthority roleAuthority = authorityMappingService.createRoleAuthority(role);

        assertThat(roleAuthority.getAuthority()).isEqualTo("ROLE_USER");
    }


    @Test
    void givenSourceApplicationIdShouldReturnGrantedAuthority() {
        Long sourceApplicationId = 2L;
        GrantedAuthority sourceApplicationAuthority = authorityMappingService.createSourceApplicationAuthority(sourceApplicationId);

        assertThat(sourceApplicationAuthority).isEqualTo(
                new SimpleGrantedAuthority("SOURCE_APPLICATION_ID_2")
        );
    }

    @Test
    void createInternalClientIdAuthority() {
        String clientId = "something";
        GrantedAuthority internalClientIdAuthority = authorityMappingService.createInternalClientIdAuthority(clientId);

        assertThat(internalClientIdAuthority.getAuthority()).isEqualTo("CLIENT_ID_something");

    }

}
