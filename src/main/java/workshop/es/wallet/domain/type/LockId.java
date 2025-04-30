package workshop.es.wallet.domain.type;

import java.util.UUID;

public record LockId(UUID value) {

    public static LockId of(String value) {
        return new LockId(UUID.fromString(value));
    }

    public static LockId of() {
        return new LockId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
