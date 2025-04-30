package workshop.es.wallet.domain.type;

import java.util.UUID;

public record EventId(UUID value) {

    public static EventId of(String value) {
        return new EventId(UUID.fromString(value));
    }

    public static EventId of() {
        return new EventId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
