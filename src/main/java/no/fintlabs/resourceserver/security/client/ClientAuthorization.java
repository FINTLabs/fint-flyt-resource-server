package no.fintlabs.resourceserver.security.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientAuthorization {
    private boolean authorized;
    private String clientId;
    private String sourceApplicationId;
}
