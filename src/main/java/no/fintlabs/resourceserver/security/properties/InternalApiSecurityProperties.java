package no.fintlabs.resourceserver.security.properties;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InternalApiSecurityProperties extends ApiSecurityProperties {

    @Value("${fint.flyt.resource-server.security.api.internal.authorized-orgid-role-pairs}")
    private String authorizedOrgIdRolePairsJson;

    private Map<String, List<String>> authorizedOrgIdRolePairs;

    @PostConstruct
    public void init() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            this.authorizedOrgIdRolePairs = mapper.readValue(authorizedOrgIdRolePairsJson, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new RuntimeException("Error parsing authorizedOrgIdRolePairs JSON", e);
        }
    }

    @Override
    public String[] getPermittedAuthorities() {
        return authorizedOrgIdRolePairs.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                        .map(role -> "ORGID_" + entry.getKey() + "_ROLE_" + role))
                .toArray(String[]::new);
    }
}
