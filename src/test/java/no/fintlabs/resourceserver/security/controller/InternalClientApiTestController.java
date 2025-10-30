package no.fintlabs.resourceserver.security.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;

import static no.fintlabs.resourceserver.UrlPaths.INTERNAL_CLIENT_API;

@RestController
@RequestMapping(INTERNAL_CLIENT_API + "/dummy")
public class InternalClientApiTestController {

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
