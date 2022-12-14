package no.fintlabs.resourceserver.security.properties;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InternalApiSecurityProperties extends ApiSecurityProperties {

    private List<String> authorizedOrgIds = Collections.emptyList();

    @Override
    public String[] getPermittedAuthorities() {
        return mapToAuthoritiesArray("ORGID_", authorizedOrgIds);
    }

}
