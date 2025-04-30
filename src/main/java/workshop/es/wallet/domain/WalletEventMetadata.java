package workshop.es.wallet.domain;

import workshop.es.wallet.domain.type.CommandId;
import workshop.es.wallet.domain.type.EventId;
import workshop.es.wallet.domain.type.WalletId;

import java.io.Serializable;
import java.time.Instant;

public record WalletEventMetadata(WalletId walletId, Instant createdAt, CommandId commandId, EventId eventId) implements Serializable {

    public WalletEventMetadata withEventId(EventId eventId) {
        return new WalletEventMetadata(walletId, createdAt, commandId, eventId);
    }
}
