package no.fintlabs.resourceserver.security.properties;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class AdminApiSecurityProperties {
    private boolean enabled = false;
    private boolean permitAll = false;
    private List<String> authorizedOrgIds = Collections.emptyList();
    private boolean requireAdminRole = true;
}
