package no.fintlabs.resourceserver.security.properties;

import lombok.*;

import java.util.Set;

@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExternalApiSecurityProperties {
    private boolean enabled;
    @Getter
    private Set<Long> authorizedSourceApplicationIds;
}
