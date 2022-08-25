package no.fintlabs.resourceserver.security;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static no.fintlabs.UrlPaths.EXTERNAL_API;

@RestController
@RequestMapping(EXTERNAL_API + "/dummy")
public class ExternalApiTestController {

    @GetMapping
    public ResponseEntity<Object> getDummy() {
        return ResponseEntity.ok().build();
    }

}
