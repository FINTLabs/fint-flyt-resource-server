package no.fintlabs.resourceserver.security.client.sourceapplication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SourceApplicationAuthorization {
    private boolean authorized;
    private String clientId;
    private Long sourceApplicationId;
}
