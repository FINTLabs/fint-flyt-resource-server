package no.fintlabs.resourceserver.security.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InternalClientApiSecurityProperties extends ApiSecurityProperties {

    private List<String> authorizedClientIds = Collections.emptyList();

    @Override
    public String[] getPermittedAuthorities() {
        return new String[0];
    }

}
