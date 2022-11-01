package no.fintlabs.resourceserver.security.properties;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import static no.fintlabs.resourceserver.security.client.sourceapplication.SourceApplicationAuthorizationUtil.SOURCE_APPLICATION_ID_PREFIX;

@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExternalApiSecurityProperties extends ApiSecurityProperties {

    private List<String> authorizedClientIds;

    @Override
    public String[] getPermittedAuthorities() {
        return mapToAuthoritiesArray(SOURCE_APPLICATION_ID_PREFIX, authorizedClientIds);
    }

}
