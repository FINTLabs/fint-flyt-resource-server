package no.fintlabs.resourceserver;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@EnableAutoConfiguration
@Configuration
@PropertySource("classpath:fint-flyt-resource-server-defaults.properties")
public class ResourceServerConfiguration {

    @Value("${fint.cache.defaultCacheEntryTimeToLiveMillis:9223372036854775807}")
    private Long defaultCacheEntryTimeToLiveMillis;

    @Value("${fint.cache.defaultCacheHeapSize:1000000}")
    private Long defaultCacheHeapSize;

    @PostConstruct
    public void configureDefaults() {
        System.setProperty("fint.cache.defaultCacheEntryTimeToLiveMillis", String.valueOf(defaultCacheEntryTimeToLiveMillis));
        System.setProperty("fint.cache.defaultCacheHeapSize", String.valueOf(defaultCacheHeapSize));
    }
}