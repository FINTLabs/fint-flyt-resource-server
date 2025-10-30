package no.fintlabs.resourceserver;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "fint.cache")
public class ResourceCacheProperties {

    private long defaultCacheEntryTimeToLiveMillis = Long.MAX_VALUE;
    private long defaultCacheHeapSize = 1_000_000L;
}