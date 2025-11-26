package no.novari.flyt.resourceserver.security;

import no.novari.flyt.resourceserver.security.client.sourceapplication.SourceApplicationAuthorizationService;
import no.novari.flyt.resourceserver.security.user.UserAuthorizationService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class AuthorizationServiceConfiguration {

    @Bean
    public SourceApplicationAuthorizationService sourceApplicationAuthorizationService(
            AuthorityMappingService authorityMappingService
    ) {
        return new SourceApplicationAuthorizationService(authorityMappingService);
    }

    @Bean
    public UserAuthorizationService userAuthorizationService(
            AuthorityMappingService authorityMappingService
    ) {
        return new UserAuthorizationService(authorityMappingService);
    }

}
