package workshop.es.wallet.domain;

import akka.javasdk.annotations.TypeName;
import workshop.es.wallet.domain.type.Amount;
import workshop.es.wallet.domain.type.Currency;
import workshop.es.wallet.domain.type.LockId;
import workshop.es.wallet.domain.type.OwnerId;
import workshop.es.wallet.domain.type.WalletId;

import java.io.Serializable;

public sealed interface WalletEvent extends Serializable {

    WalletEventMetadata metadata();

    default WalletId walletId() {
        return metadata().walletId();
    }

    @TypeName("wallet-created")
    record WalletCreated(WalletEventMetadata metadata, OwnerId ownerId, Currency currency,
                         Amount amount) implements WalletEvent {
    }

    @TypeName("funds-deposited")
    record FundsDeposited(WalletEventMetadata metadata, OwnerId ownerId, Currency currency,
                          Amount depositAmount, Amount balanceAfter) implements WalletEvent {
    }

    @TypeName("funds-withdrawn")
    record FundsWithdrawn(WalletEventMetadata metadata, OwnerId ownerId, Currency currency, Amount withdrawAmount,
                          Amount balanceAfter) implements WalletEvent {
    }

    @TypeName("transfer-funds-locked")
    record TransferFundsLocked(WalletEventMetadata metadata, OwnerId ownerId, Currency currency, Amount transferAmount,
                               Amount balanceAfter,
                               WalletId destination, LockId lockId) implements WalletEvent {
    }

    @TypeName("transfer-funds-aborted")
    record TransferFundsAborted(WalletEventMetadata metadata, OwnerId ownerId, Currency currency, LockId lockId,
                                String reason) implements WalletEvent {
    }

    @TypeName("transfer-funds-withdrawn")
    record TransferFundsWithdrawn(WalletEventMetadata metadata, OwnerId ownerId, Currency currency,
                                  Amount transferAmount, Amount balanceAfter, LockId lockId) implements WalletEvent {
    }

    @TypeName("transfer-funds-deposited")
    record TransferFundsDeposited(WalletEventMetadata metadata, OwnerId ownerId, Currency currency,
                                  Amount transferAmount, Amount balanceAfter, WalletId source,
                                  LockId sourceLockId) implements WalletEvent {
    }
}
