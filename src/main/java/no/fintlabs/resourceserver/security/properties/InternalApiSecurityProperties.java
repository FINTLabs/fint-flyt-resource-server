package no.fintlabs.resourceserver.security.properties;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import no.fintlabs.resourceserver.security.user.UserRole;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class InternalApiSecurityProperties {
    private boolean enabled;
    private String authorizedOrgIdRolePairsJson;
    @Getter
    private Map<String, Set<UserRole>> userRoleFilterPerOrgId;

    @PostConstruct
    public void parseAndSetAuthorizedOrgIdRolePairs() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            this.userRoleFilterPerOrgId = mapper.readValue(authorizedOrgIdRolePairsJson, new TypeReference<>() {
            });
            log.info("Parsed authorizedOrgIdRolePairs: {}", userRoleFilterPerOrgId);
        } catch (IOException e) {
            log.error("Error parsing authorizedOrgIdRolePairsJson: {}", e.getMessage(), e);
        }
    }

}
