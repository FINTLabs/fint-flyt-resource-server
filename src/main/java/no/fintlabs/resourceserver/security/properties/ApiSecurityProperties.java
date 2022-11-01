package no.fintlabs.resourceserver.security.properties;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public abstract class ApiSecurityProperties {

    private boolean enabled = false;
    private boolean permitAll = false;

    public abstract String[] getPermittedAuthorities();

    protected String[] mapToAuthoritiesArray(String prefix, List<String> values) {
        return values
                .stream()
                .map(id -> prefix + id)
                .toArray(String[]::new);
    }

}
