package no.fintlabs.resourceserver.security.user;

import no.fintlabs.resourceserver.security.properties.InternalApiSecurityProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DirtiesContext
class UserRoleFilteringServiceTest {

    @Mock
    private InternalApiSecurityProperties internalApiSecurityProperties;

    @InjectMocks
    private UserRoleFilteringService userRoleFilteringService;

    @Test
    void givenNoRolesShouldReturnNoUserRoles() {
        List<String> roleStringList = List.of();
        String organizationId = "testOrganizationId";

        Set<UserRole> filter = userRoleFilteringService.filter(roleStringList, organizationId);

        assertThat(filter).isEmpty();
        verifyNoInteractions(internalApiSecurityProperties);
    }

    @Test
    void givenUnknownRolesShouldReturnNoUserRoles() {
        List<String> roleStringList = List.of("unknownRole1", "unknownRole2");
        String organizationId = "testOrganizationId";

        Set<UserRole> filter = userRoleFilteringService.filter(roleStringList, organizationId);

        assertThat(filter).isEmpty();
        verifyNoInteractions(internalApiSecurityProperties);
    }

    @Test
    void givenKnownRoleAndApprovedForOrganizationShouldReturnUserRole() {
        List<String> roleStringList = List.of(UserRole.USER.getRoleValue());
        String organizationId = "testOrganizationId";

        Map<String, Set<UserRole>> userRolesFilterPerOrgId = Map.of(organizationId, Set.of(UserRole.USER));
        when(internalApiSecurityProperties.getUserRoleFilterPerOrgId()).thenReturn(userRolesFilterPerOrgId);

        Set<UserRole> filter = userRoleFilteringService.filter(roleStringList, organizationId);

        assertThat(filter).isEqualTo(Set.of(UserRole.USER));
    }

    @Test
    void givenKnownRoleAndNoApprovedRolesForOrganizationShouldReturnNoUserRoles() {
        List<String> roleStringList = List.of(UserRole.USER.getRoleValue());
        String organizationId = "testOrganizationId";

        Map<String, Set<UserRole>> userRolesFilterPerOrgId = Map.of(organizationId, Set.of());
        when(internalApiSecurityProperties.getUserRoleFilterPerOrgId()).thenReturn(userRolesFilterPerOrgId);

        Set<UserRole> filter = userRoleFilteringService.filter(roleStringList, organizationId);

        assertThat(filter).isEmpty();
    }

    @Test
    void givenKnownRoleAndNoFilterForOrganizationShouldReturnNoUserRoles() {
        List<String> roleStringList = List.of(UserRole.USER.getRoleValue());
        String organizationId = "testOrganizationId1";

        Map<String, Set<UserRole>> userRolesFilterPerOrgId = Map.of("testOrganizationId2", Set.of(UserRole.USER));
        when(internalApiSecurityProperties.getUserRoleFilterPerOrgId()).thenReturn(userRolesFilterPerOrgId);

        Set<UserRole> filter = userRoleFilteringService.filter(roleStringList, organizationId);

        assertThat(filter).isEmpty();
    }

    @Test
    void givenKnownApprovedAndKnownUnapprovedAndUnknownRolesShouldReturnOnlyKnownApprovedUserRoles() {
        List<String> roleStringList = List.of(
                UserRole.ADMIN.getRoleValue(),
                UserRole.USER.getRoleValue(),
                "unknownRole1"
        );
        String organizationId = "testOrganizationId1";

        Map<String, Set<UserRole>> userRolesFilterPerOrgId = Map.of(
                organizationId, Set.of(UserRole.ADMIN),
                "testOrganizationId2", Set.of(UserRole.ADMIN, UserRole.USER)
        );
        when(internalApiSecurityProperties.getUserRoleFilterPerOrgId()).thenReturn(userRolesFilterPerOrgId);

        Set<UserRole> filter = userRoleFilteringService.filter(roleStringList, organizationId);

        assertThat(filter).isEqualTo(Set.of(UserRole.ADMIN));
    }

}
