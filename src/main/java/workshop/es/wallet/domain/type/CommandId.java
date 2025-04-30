package workshop.es.wallet.domain.type;

import java.util.UUID;

public record CommandId(UUID value)  {

    public static CommandId of() {
        return of(UUID.randomUUID());
    }

    public static CommandId of(String value) {
        return of(UUID.fromString(value));
    }

    public static CommandId of(UUID value) {
        return new CommandId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
