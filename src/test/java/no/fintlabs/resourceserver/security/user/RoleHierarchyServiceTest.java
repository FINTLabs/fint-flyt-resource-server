package no.fintlabs.resourceserver.security.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RoleHierarchyServiceTest {

    private RoleHierarchyService roleHierarchyService;

    @BeforeEach
    void setUp() {
        roleHierarchyService = new RoleHierarchyService();
    }

    @Test
    void givenUserShouldReturnUser() {
        assertThat(roleHierarchyService.getProvidedAndImpliedRoles(List.of(UserRole.USER)))
                .isEqualTo(
                        Set.of(UserRole.USER)
                );
    }

    @Test
    void givenAdminShouldReturnAdminAndUser() {
        assertThat(roleHierarchyService.getProvidedAndImpliedRoles(List.of(UserRole.ADMIN)))
                .isEqualTo(
                        Set.of(UserRole.ADMIN, UserRole.USER)
                );
    }

    @Test
    void givenDeveloperShouldReturnDeveloperAndAdminAndUser() {
        assertThat(roleHierarchyService.getProvidedAndImpliedRoles(List.of(UserRole.DEVELOPER)))
                .isEqualTo(
                        Set.of(UserRole.DEVELOPER, UserRole.ADMIN, UserRole.USER)
                );
    }

}
