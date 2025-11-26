package no.novari.flyt.resourceserver.security.client.sourceapplication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SourceApplicationAuthorization {
    private boolean authorized;
    private String clientId;
    private Long sourceApplicationId;
}
