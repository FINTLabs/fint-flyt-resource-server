package no.novari.flyt.resourceserver.security;

import no.novari.flyt.resourceserver.security.client.internal.InternalClientAuthorityMappingService;
import no.novari.flyt.resourceserver.security.client.internal.InternalClientJwtConverter;
import no.novari.flyt.resourceserver.security.properties.InternalClientApiSecurityProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnProperty(
        prefix = "novari.flyt.resource-server.security.api",
        value = "internal-client.enabled",
        havingValue = "true"
)
public class InternalClientApiConfiguration {

    @Bean
    @ConfigurationProperties("novari.flyt.resource-server.security.api.internal-client")
    InternalClientApiSecurityProperties internalClientApiSecurityProperties() {
        return new InternalClientApiSecurityProperties();
    }

    @Bean
    InternalClientAuthorityMappingService internalClientAuthorityMappingService(
            AuthorityMappingService authorityMappingService
    ) {
        return new InternalClientAuthorityMappingService(authorityMappingService);
    }

    @Bean
    InternalClientJwtConverter internalClientJwtConverter(
            InternalClientAuthorityMappingService internalClientAuthorityMappingService
    ) {
        return new InternalClientJwtConverter(internalClientAuthorityMappingService);
    }

}
