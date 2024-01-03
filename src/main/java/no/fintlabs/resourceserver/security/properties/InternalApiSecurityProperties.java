package no.fintlabs.resourceserver.security.properties;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InternalApiSecurityProperties extends ApiSecurityProperties {

    private Map<String, List<String>> authorizedOrgIdRolePairs = Collections.emptyMap();

    @Override
    public String[] getPermittedAuthorities() {
        return authorizedOrgIdRolePairs.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                        .map(role -> "ORGID_" + entry.getKey() + "_ROLE_" + role))
                .toArray(String[]::new);
    }
}
