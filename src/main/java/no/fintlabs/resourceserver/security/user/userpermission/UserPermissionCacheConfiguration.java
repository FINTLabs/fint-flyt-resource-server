package no.fintlabs.resourceserver.security.user.userpermission;

import no.fintlabs.cache.FintCache;
import no.fintlabs.cache.FintCacheConfiguration;
import no.fintlabs.cache.FintCacheManager;
import no.fintlabs.cache.FintCacheOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.time.Duration;
import java.util.UUID;

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
    FintCache<UUID, UserPermission> userPermissionCache() {
        return fintCacheManager.createCache(
                "userpermission",
                UUID.class,
                UserPermission.class,
                FintCacheOptions
                        .builder()
                        .timeToLive(Duration.ofMillis(9223372036854775807L))
                        .heapSize(1000000L)
                        .build()
        );
    }
}