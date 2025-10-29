package no.fintlabs.resourceserver.security.user.userpermission;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class UserPermission {
    private UUID objectIdentifier;
    private List<Long> sourceApplicationIds; // TODO: Change to set

    @JsonCreator
    public UserPermission(@JsonProperty("objectIdentifier") UUID objectIdentifier,
                          @JsonProperty("sourceApplicationIds") List<Long> sourceApplicationIds) {
        this.objectIdentifier = objectIdentifier;
        this.sourceApplicationIds = sourceApplicationIds;
    }
}
