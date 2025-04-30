package workshop.es.wallet.domain;

import workshop.es.wallet.domain.type.Amount;
import workshop.es.wallet.domain.type.LockId;
import workshop.es.wallet.domain.type.WalletId;

public record TransferLock(LockId id, WalletId destination, Amount amount) {
}
