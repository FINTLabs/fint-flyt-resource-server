package no.fintlabs.resourceserver.security;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static no.fintlabs.resourceserver.UrlPaths.INTERNAL_CLIENT_API;

@RestController
@RequestMapping(INTERNAL_CLIENT_API + "/dummy")
public class InternalClientApiTestController {

    @GetMapping
    public ResponseEntity<Object> getDummy() {
        return ResponseEntity.ok().build();
    }

}
