package no.fintlabs.resourceserver.security.properties;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class ExternalApiSecurityProperties {
    private boolean enabled = false;
    private boolean permitAll = false;
    private List<String> authorizedClientIds = Collections.emptyList();
}
