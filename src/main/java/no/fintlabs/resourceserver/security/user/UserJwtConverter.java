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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public class UserJwtConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    private final FintCache<UUID, UserPermission> userPermissionCache;
    private final UserRoleFilteringService userRoleFilteringService;
    private final SourceApplicationAuthorityMappingService sourceApplicationAuthorityMappingService;
    private final RoleAuthorityMappingService roleAuthorityMappingService;

    @Nonnull
    public Mono<AbstractAuthenticationToken> convert(@Nonnull Jwt jwt) {
        try {
            String organizationId = Optional.ofNullable(
                    jwt.getClaimAsString(UserClaim.ORGANIZATION_ID.getJwtTokenClaimName())
            ).orElseThrow(() -> new IllegalArgumentException("Missing Claim: " + UserClaim.ORGANIZATION_ID));
            log.debug("Extracted organization ID from JWT: {}", organizationId);

            String objectIdentifierString = Optional.ofNullable(
                    jwt.getClaimAsString(UserClaim.OBJECT_IDENTIFIER.getJwtTokenClaimName())
            ).orElseThrow(() -> new IllegalArgumentException("Missing Claim: " + UserClaim.OBJECT_IDENTIFIER));
            log.debug("Extracted objectIdentifier from JWT: {}", objectIdentifierString);

            UUID objectIdentifier = UUID.fromString(objectIdentifierString);

            Set<GrantedAuthority> authorities = new HashSet<>();

            userPermissionCache.getOptional(objectIdentifier)
                    .map(UserPermission::getSourceApplicationIds)
                    .map(sourceApplicationAuthorityMappingService::createSourceApplicationAuthorities)
                    .ifPresent(authorities::addAll);

            Set<String> roleValues = Set.copyOf(jwt.getClaimAsStringList(UserClaim.ROLES.getJwtTokenClaimName()));
            log.debug("Extracted roles from JWT: {}", roleValues);
            if (!roleValues.isEmpty()) {
                Set<UserRole> filteredUserRoles = userRoleFilteringService.filter(roleValues, organizationId);
                authorities.addAll(roleAuthorityMappingService.createRoleAuthorities(filteredUserRoles));
            }
            return Mono.just(new JwtAuthenticationToken(jwt, authorities));
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

}
