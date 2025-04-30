package workshop.es.wallet.domain;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import workshop.es.base.domain.Clock;
import workshop.es.wallet.domain.type.Amount;
import workshop.es.wallet.domain.type.CommandId;
import workshop.es.wallet.domain.type.Currency;
import workshop.es.wallet.domain.type.EventId;
import workshop.es.wallet.domain.type.LockId;
import workshop.es.wallet.domain.type.OwnerId;
import workshop.es.wallet.domain.type.WalletId;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = WalletCommand.CreateWallet.class, name = "create"),
    @JsonSubTypes.Type(value = WalletCommand.DepositFunds.class, name = "deposit"),
    @JsonSubTypes.Type(value = WalletCommand.WithdrawFunds.class, name = "withdraw"),
    @JsonSubTypes.Type(value = WalletCommand.TransferFunds.class, name = "transfer"),
    @JsonSubTypes.Type(value = WalletCommand.ConfirmTransferFunds.class, name = "confirm-transfer"),
    @JsonSubTypes.Type(value = WalletCommand.AbortTransferFunds.class, name = "abort-transfer"),
    @JsonSubTypes.Type(value = WalletCommand.DepositTransferFunds.class, name = "deposit-transfer")})
public sealed interface WalletCommand {

    WalletId walletId();

    CommandId commandId();

    default WalletEventMetadata toEventMetadata(Clock clock) {
        return new WalletEventMetadata(walletId(), clock.now(), commandId(), EventId.of());
    }

    record CreateWallet(WalletId walletId, CommandId commandId, OwnerId ownerId,
                        Currency currency) implements WalletCommand {
    }

    record DepositFunds(WalletId walletId, CommandId commandId, OwnerId ownerId, Currency currency,
                        Amount amount) implements WalletCommand {
    }

    record WithdrawFunds(WalletId walletId, CommandId commandId, OwnerId ownerId, Currency currency,
                         Amount amount) implements WalletCommand {
    }

    record TransferFunds(WalletId walletId, CommandId commandId, OwnerId ownerId, Currency currency,
                         Amount amount, WalletId destination) implements WalletCommand {
    }

    record AbortTransferFunds(WalletId walletId, CommandId commandId, Currency currency,
                              LockId lockId, String reason) implements WalletCommand {
    }

    record ConfirmTransferFunds(WalletId walletId, CommandId commandId, Currency currency,
                                LockId lockId) implements WalletCommand {
    }

    record DepositTransferFunds(WalletId walletId, CommandId commandId, Currency currency,
                                Amount amount, WalletId source, LockId sourceLockId) implements WalletCommand {
    }
}
