package no.fintlabs.resourceserver.security.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.cache.FintCache;
import no.fintlabs.resourceserver.security.AuthorityMappingService;
import no.fintlabs.resourceserver.security.properties.InternalApiSecurityProperties;
import no.fintlabs.resourceserver.security.user.userpermission.UserPermission;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserJwtConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    private final FintCache<String, UserPermission> userPermissionCache;
    private final AuthorityMappingService authorityMappingService;
    private final InternalApiSecurityProperties internalApiSecurityProperties;

    public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {
        String organizationId = Optional.ofNullable(
                jwt.getClaimAsString(UserClaim.ORGANIZATION_ID.getJwtTokenClaimName())
        ).orElseThrow(() -> new IllegalArgumentException("Missing Claim: " + UserClaim.ORGANIZATION_ID));
        log.debug("Extracted organization ID from JWT: {}", organizationId);

        String objectIdentifier = Optional.ofNullable(
                jwt.getClaimAsString(UserClaim.OBJECT_IDENTIFIER.getJwtTokenClaimName())
        ).orElseThrow(() -> new IllegalArgumentException("Missing Claim: " + UserClaim.OBJECT_IDENTIFIER));
        log.debug("Extracted objectIdentifier from JWT: {}", objectIdentifier);

        List<String> rolesStringList = jwt.getClaimAsStringList(UserClaim.ROLES.getJwtTokenClaimName());
        log.debug("Extracted roles from JWT: {}", rolesStringList);

        Set<UserRole> userRoles = rolesStringList
                .stream()
                .map(UserRole::getUserRoleFromValue)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        List<GrantedAuthority> authorities = new ArrayList<>();

        userPermissionCache.getOptional(objectIdentifier)
                .map(UserPermission::getSourceApplicationIds)
                .map(authorityMappingService::createSourceApplicationAuthorities)
                .ifPresent(authorities::addAll);

        Set<UserRole> roleFilter = internalApiSecurityProperties.getUserRoleFilterPerOrgId()
                .getOrDefault(organizationId, Collections.emptySet());

        Set<UserRole> filteredRoles = userRoles;
        filteredRoles.retainAll(roleFilter);
        authorities.addAll(authorityMappingService.createRoleAuthorities(filteredRoles));

        return Mono.just(new JwtAuthenticationToken(jwt, authorities));
    }

}
