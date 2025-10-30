package no.fintlabs.resourceserver.security.properties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InternalClientApiSecurityProperties {
    private boolean enabled;
    private Set<String> authorizedClientIds;
}
