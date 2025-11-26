package no.novari.flyt.resourceserver.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Set;

import static no.novari.flyt.resourceserver.security.AuthorityMappingService.AUTHORITY_DELIMITER;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DirtiesContext
public class AuthorityMappingServiceTest {

    @InjectMocks
    private AuthorityMappingService authorityMappingService;

    @Test
    void givenAuthorityPrefixAndStringValueShouldReturnStringOfAuthorityPrefix() {
        AuthorityPrefix prefix = AuthorityPrefix.CLIENT_ID;
        String value = "testAuthorityValue";
        String authorityString = authorityMappingService.toAuthority(prefix, value);

        assertThat(authorityString).isEqualTo("CLIENT_ID_testAuthorityValue");
    }

    @Test
    void givenAuthorityPrefixAndCollectionOfGrantedAuthoritiesShouldReturnSetOfLongAuthorityValues() {
        AuthorityPrefix prefix1 = AuthorityPrefix.CLIENT_ID;
        AuthorityPrefix prefix2 = AuthorityPrefix.ROLE;

        Set<GrantedAuthority> authorities = Set.of(
                new SimpleGrantedAuthority(prefix1.getValue() + AUTHORITY_DELIMITER + 1L),
                new SimpleGrantedAuthority(prefix1.getValue() + AUTHORITY_DELIMITER + 2L),
                new SimpleGrantedAuthority(prefix2.getValue() + AUTHORITY_DELIMITER + 3L),
                new SimpleGrantedAuthority(String.valueOf(4L))
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

        Set<SimpleGrantedAuthority> authorities = Set.of(
                new SimpleGrantedAuthority(prefix1.getValue() + AUTHORITY_DELIMITER + "a"),
                new SimpleGrantedAuthority(prefix1.getValue() + AUTHORITY_DELIMITER + "b"),
                new SimpleGrantedAuthority(prefix2.getValue() + AUTHORITY_DELIMITER + "c"),
                new SimpleGrantedAuthority("d")
        );
        Set<String> strings = authorityMappingService.extractStringValues(AuthorityPrefix.CLIENT_ID, authorities);

        assertThat(strings).isEqualTo(
                Set.of("a", "b")
        );
    }

}
