package no.fintlabs.resourceserver.security.integration.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;

import static no.fintlabs.resourceserver.UrlPaths.INTERNAL_ADMIN_API;

@RestController
@RequestMapping(INTERNAL_ADMIN_API + "/dummy")
public class InternalAdminApiTestController {

    @GetMapping
    public Mono<Set<String>> getDummy(Authentication authentication) {
        if (authentication == null) {
            return Mono.empty();
        }
        return Mono.just(
                authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet())
        );
    }

}
