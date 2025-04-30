package workshop.es.base.application;

import workshop.es.base.domain.Clock;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class UTCClock implements Clock {

    @Override
    public Instant now() {
        return Instant.now().truncatedTo(ChronoUnit.MILLIS);
    }
}
