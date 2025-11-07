package no.novari.flyt.resourceserver.security.client.sourceapplication.exceptions;

import java.util.Collection;
import java.util.stream.Collectors;

public class MultipleSourceApplicationIdsException extends RuntimeException {

    public MultipleSourceApplicationIdsException(Collection<Long> sourceApplicationIds) {
        super("Source application IDs: " +
              sourceApplicationIds
                      .stream()
                      .map(String::valueOf)
                      .collect(Collectors.joining(", ", "[", "]"))
        );
    }

}
