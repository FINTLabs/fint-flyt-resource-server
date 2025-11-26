package no.novari.flyt.resourceserver.security.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserRoleHierarchyServiceTest {

    private UserRoleHierarchyService userRoleHierarchyService;

    @BeforeEach
    void setUp() {
        userRoleHierarchyService = new UserRoleHierarchyService();
    }

    @Test
    void givenUserShouldReturnUser() {
        assertThat(userRoleHierarchyService.getProvidedAndImpliedRoles(List.of(UserRole.USER)))
                .isEqualTo(
                        Set.of(UserRole.USER)
                );
    }

    @Test
    void givenAdminShouldReturnAdminAndUser() {
        assertThat(userRoleHierarchyService.getProvidedAndImpliedRoles(List.of(UserRole.ADMIN)))
                .isEqualTo(
                        Set.of(UserRole.ADMIN, UserRole.USER)
                );
    }

    @Test
    void givenDeveloperShouldReturnDeveloperAndAdminAndUser() {
        assertThat(userRoleHierarchyService.getProvidedAndImpliedRoles(List.of(UserRole.DEVELOPER)))
                .isEqualTo(
                        Set.of(UserRole.DEVELOPER, UserRole.ADMIN, UserRole.USER)
                );
    }

}
