package no.fintlabs.resourceserver.security.properties;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

import static no.fintlabs.resourceserver.security.client.ClientAuthorizationUtil.CLIENT_ID_PREFIX;

@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InternalClientApiSecurityProperties extends ApiSecurityProperties {

    private List<String> authorizedClientIds = Collections.emptyList();

    @Override
    public String[] getPermittedAuthorities() {
        return mapToAuthoritiesArray(CLIENT_ID_PREFIX, authorizedClientIds);
    }

}
