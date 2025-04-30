package workshop.es.base.domain;

import java.time.Instant;

public interface Clock {

    Instant now();

    default Instant from(long timestamp) {
        return Instant.ofEpochMilli(timestamp);
    }
}
