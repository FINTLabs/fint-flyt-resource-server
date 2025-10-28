package no.fintlabs.resourceserver.security.properties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InternalClientApiSecurityProperties {
    private boolean enabled;
    private List<String> authorizedClientIds;
}
