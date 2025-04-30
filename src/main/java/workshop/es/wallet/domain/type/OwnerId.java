package workshop.es.wallet.domain.type;

import java.util.UUID;

public record OwnerId(String value) {

    public static OwnerId of() {
        return new OwnerId(UUID.randomUUID().toString());
    }

    public static OwnerId of(String value) {
        return new OwnerId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
