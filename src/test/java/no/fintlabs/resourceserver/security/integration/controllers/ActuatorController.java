package no.fintlabs.resourceserver.security.integration.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Set;

@RestController
@RequestMapping("/actuator")
public class ActuatorController {

    @GetMapping("/dummy")
    public Mono<Set<String>> getDummy() {
        return Mono.just(Set.of("OK"));
    }

}
