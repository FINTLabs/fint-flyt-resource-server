package no.fintlabs.resourceserver.security.properties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExternalApiSecurityProperties {
    private boolean enabled;
    private Set<Long> authorizedSourceApplicationIds;
}
