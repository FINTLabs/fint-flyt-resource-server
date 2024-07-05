package no.fintlabs.resourceserver.security.userpermission;

import no.fintlabs.cache.FintCache;
import no.fintlabs.cache.FintCacheConfiguration;
import no.fintlabs.cache.FintCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(FintCacheConfiguration.class)
public class UserPermissionCacheConfiguration {

    private final FintCacheManager fintCacheManager;

    public UserPermissionCacheConfiguration(
            FintCacheManager fintCacheManager
    ) {
        this.fintCacheManager = fintCacheManager;
    }

    @Bean
    FintCache<String, UserPermission> userPermissionCache() {
        return fintCacheManager.createCache(
                "userpermission",
                String.class,
                UserPermission.class
        );
    }
}