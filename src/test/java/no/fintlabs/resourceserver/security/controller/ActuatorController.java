package no.fintlabs.resourceserver.security.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/actuator")
public class ActuatorController {

    @GetMapping("/dummy")
    public String getDummy() {
        return "OK";
    }

}
