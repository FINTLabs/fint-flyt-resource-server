package no.novari.flyt.resourceserver.security.user.permission;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;
import java.util.UUID;

@Getter
@Builder
public class UserPermission {
    private UUID objectIdentifier;
    private Set<Long> sourceApplicationIds;

    @JsonCreator
    public UserPermission(@JsonProperty("objectIdentifier") UUID objectIdentifier,
                          @JsonProperty("sourceApplicationIds") Set<Long> sourceApplicationIds) {
        this.objectIdentifier = objectIdentifier;
        this.sourceApplicationIds = sourceApplicationIds;
    }
}
