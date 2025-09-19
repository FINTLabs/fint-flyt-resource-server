package no.fintlabs.resourceserver.security;

import no.fintlabs.resourceserver.security.client.sourceapplication.SourceApplicationAuthorizationRequestService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

@TestConfiguration(proxyBeanMethods = false)
public class TestMocksConfiguration {

    @Bean
    ReactiveJwtDecoder reactiveJwtDecoder() {
        return Mockito.mock(ReactiveJwtDecoder.class);
    }

    @Bean
    SourceApplicationAuthorizationRequestService sourceApplicationAuthorizationRequestService() {
        return Mockito.mock(SourceApplicationAuthorizationRequestService.class);
    }
}
