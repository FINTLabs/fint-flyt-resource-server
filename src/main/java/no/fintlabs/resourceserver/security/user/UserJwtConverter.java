package no.fintlabs.resourceserver.security.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.cache.FintCache;
import no.fintlabs.resourceserver.security.AuthorityMappingService;
import no.fintlabs.resourceserver.security.user.userpermission.UserPermission;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserJwtConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    private final FintCache<String, UserPermission> userPermissionCache;
    private final AuthorityMappingService authorityMappingService;

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
        List<UserRole> userRoles = rolesStringList
                .stream()
                .map(UserRole::getUserRoleFromValue)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        log.debug("Extracted roles from JWT: {}", rolesStringList);

        List<GrantedAuthority> authorities = new ArrayList<>();

//        userPermissionCache.getOptional(objectIdentifier)
//                .map(UserPermission::getSourceApplicationIds)
//                .map(authorityMappingService::createSourceApplicationAuthorities)
//                .ifPresent(authorities::addAll);

        // TODO 17/10/2025 eivindmorch: Replace with and-logic in sec filter?
        for (UserRole role : userRoles) {
//            GrantedAuthority orgAndRoleAuthority = authorityMappingService.createOrgAndRoleAuthority(organizationId, role);
//            log.debug("orgIdAndRoleGrantedAuthorityString: {}", orgAndRoleAuthority);
//            authorities.add(orgAndRoleAuthority);
        }
        return Mono.just(new JwtAuthenticationToken(jwt, authorities));
    }

}
