package no.fintlabs.resourceserver.security.properties;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class InternalApiSecurityProperties extends ApiSecurityProperties {

    private String authorizedOrgIdRolePairsJson = "{}";
    private String adminRole = "";

    private Map<String, List<String>> authorizedOrgIdRolePairs = Collections.emptyMap();

    @PostConstruct
    public void parseAndSetAuthorizedOrgIdRolePairs() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            this.authorizedOrgIdRolePairs = mapper.readValue(authorizedOrgIdRolePairsJson,
                    new TypeReference<>() {
                    });
        } catch (IOException e) {
            log.error("Error parsing authorizedOrgIdRolePairsJson: {}", e.getMessage(), e);
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
