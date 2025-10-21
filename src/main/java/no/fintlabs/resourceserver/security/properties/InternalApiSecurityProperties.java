package no.fintlabs.resourceserver.security.properties;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.resourceserver.security.user.UserRole;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class InternalApiSecurityProperties {
    private boolean enabled;
    private String authorizedOrgIdRolePairsJson = "{}";
    private Map<String, List<UserRole>> userRolesPerOrgId = Collections.emptyMap();

    @PostConstruct
    public void parseAndSetAuthorizedOrgIdRolePairs() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            this.userRolesPerOrgId = mapper.readValue(authorizedOrgIdRolePairsJson, new TypeReference<>() {
            });
            log.info("Parsed authorizedOrgIdRolePairs: {}", userRolesPerOrgId);
        } catch (IOException e) {
            log.error("Error parsing authorizedOrgIdRolePairsJson: {}", e.getMessage(), e);
        }
    }

}
