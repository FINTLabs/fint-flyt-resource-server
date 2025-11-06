package no.fintlabs.resourceserver.security.user;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.cache.FintCache;
import no.fintlabs.resourceserver.security.RoleAuthorityMappingService;
import no.fintlabs.resourceserver.security.SourceApplicationAuthorityMappingService;
import no.fintlabs.resourceserver.security.user.userpermission.UserPermission;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public class UserJwtConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    private final FintCache<UUID, UserPermission> userPermissionCache;
    private final UserRoleFilteringService userRoleFilteringService;
    private final SourceApplicationAuthorityMappingService sourceApplicationAuthorityMappingService;
    private final RoleHierarchyService roleHierarchyService;
    private final RoleAuthorityMappingService roleAuthorityMappingService;

    @Nonnull
    public Mono<AbstractAuthenticationToken> convert(@Nonnull Jwt jwt) {
        try {
            String organizationId = jwt.getClaimAsString(UserClaim.ORGANIZATION_ID.getTokenClaimName());
            log.debug("Extracted organization ID from JWT: {}", organizationId);

            String objectIdentifierString = jwt.getClaimAsString(UserClaim.OBJECT_IDENTIFIER.getTokenClaimName());
            log.debug("Extracted objectIdentifier from JWT: {}", objectIdentifierString);

            if (organizationId == null || objectIdentifierString == null) {
                return Mono.just(new JwtAuthenticationToken(jwt));
            }

            UUID objectIdentifier = UUID.fromString(objectIdentifierString);

            Set<GrantedAuthority> authorities = new HashSet<>();

            userPermissionCache.getOptional(objectIdentifier)
                    .map(UserPermission::getSourceApplicationIds)
                    .map(sourceApplicationAuthorityMappingService::createSourceApplicationAuthorities)
                    .ifPresent(authorities::addAll);

            Set<String> roleValues = Set.copyOf(jwt.getClaimAsStringList(UserClaim.ROLES.getTokenClaimName()));
            log.debug("Extracted roles from JWT: {}", roleValues);
            if (!roleValues.isEmpty()) {
                Set<UserRole> filteredUserRoles = userRoleFilteringService.filter(roleValues, organizationId);
                Set<UserRole> providedAndImpliedRoles = roleHierarchyService.getProvidedAndImpliedRoles(filteredUserRoles);
                authorities.addAll(roleAuthorityMappingService.createRoleAuthorities(providedAndImpliedRoles));
            }
            return Mono.just(new JwtAuthenticationToken(jwt, authorities));
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

}
