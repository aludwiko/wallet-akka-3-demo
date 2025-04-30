package workshop.es.wallet.domain;

import workshop.es.wallet.domain.WalletCommand.AbortTransferFunds;
import workshop.es.wallet.domain.WalletCommand.ConfirmTransferFunds;
import workshop.es.wallet.domain.WalletCommand.CreateWallet;
import workshop.es.wallet.domain.WalletCommand.DepositFunds;
import workshop.es.wallet.domain.WalletCommand.DepositTransferFunds;
import workshop.es.wallet.domain.WalletCommand.TransferFunds;
import workshop.es.wallet.domain.WalletCommand.WithdrawFunds;
import workshop.es.wallet.domain.type.Amount;
import workshop.es.wallet.domain.type.Currency;
import workshop.es.wallet.domain.type.OwnerId;
import workshop.es.wallet.domain.type.WalletId;
import io.vavr.collection.List;
import io.vavr.control.Either;
import workshop.es.base.domain.Clock;
import workshop.es.wallet.domain.WalletEvent.FundsDeposited;
import workshop.es.wallet.domain.WalletEvent.FundsWithdrawn;
import workshop.es.wallet.domain.WalletEvent.TransferFundsAborted;
import workshop.es.wallet.domain.WalletEvent.TransferFundsDeposited;
import workshop.es.wallet.domain.WalletEvent.TransferFundsLocked;
import workshop.es.wallet.domain.WalletEvent.TransferFundsWithdrawn;
import workshop.es.wallet.domain.WalletEvent.WalletCreated;
import workshop.es.wallet.domain.type.LockId;

import java.io.Serializable;
import java.math.BigDecimal;

import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;
import static workshop.es.wallet.domain.WalletCommandError.CURRENCY_NOT_SUPPORTED;
import static workshop.es.wallet.domain.WalletCommandError.LOCK_NOT_FOUND;
import static workshop.es.wallet.domain.WalletCommandError.NOT_AUTHORIZED;
import static workshop.es.wallet.domain.WalletCommandError.NOT_SUFFICIENT_FUNDS;
import static workshop.es.wallet.domain.WalletCommandError.WALLET_ALREADY_CREATED;
import static workshop.es.wallet.domain.WalletCommandError.WALLET_NOT_CREATED;
import static workshop.es.wallet.domain.WalletCommandError.WRONG_CURRENCY;

public record Wallet(WalletId id, OwnerId ownerId, Currency currency, Amount balance,
                     List<TransferLock> transferLocks) implements Serializable {

    public static final Wallet EMPTY = new Wallet(null, null, null, null, null);

    public static final Amount INITIAL_AMOUNT = new Amount(BigDecimal.ZERO);

    private static boolean isSupported(Currency currency) {
        //more domain validation here :)
        return true;
    }

    public Either<WalletCommandError, List<WalletEvent>> process(WalletCommand walletCommand, Clock clock) {
        return switch (walletCommand) {
            case CreateWallet createWallet -> handleCreation(createWallet, clock);
            case DepositFunds depositFunds -> handleDeposit(depositFunds, clock);
            case WithdrawFunds withdrawFunds -> handleWithdraw(withdrawFunds, clock);
            case TransferFunds transferFunds -> handleTransferFunds(transferFunds, clock);
            case AbortTransferFunds abortTransferFunds -> handleAbortTransfer(abortTransferFunds, clock);
            case ConfirmTransferFunds confirmTransferFunds -> handleConfirmTransfer(confirmTransferFunds, clock);
            case DepositTransferFunds depositTransferFunds -> handleTransferDeposit(depositTransferFunds, clock);
        };
    }

    private boolean isEmpty() {
        return this.equals(EMPTY);
    }

    private Either<WalletCommandError, List<WalletEvent>> handleTransferDeposit(DepositTransferFunds depositTransferFunds, Clock clock) {
        if (isEmpty()) {
            return left(WALLET_NOT_CREATED);
        }
        TransferFundsDeposited transferFundsDeposited = new TransferFundsDeposited(depositTransferFunds.toEventMetadata(clock), ownerId, currency,
            depositTransferFunds.amount(), depositTransferFunds.amount().add(balance),  // TODO: check if this is correct
            depositTransferFunds.source(), depositTransferFunds.sourceLockId());
        return right(List.of(transferFundsDeposited));
    }

    private Either<WalletCommandError, List<WalletEvent>> handleConfirmTransfer(ConfirmTransferFunds confirmTransferFunds, Clock clock) {
        //more validation here
        if (isEmpty()) {
            return left(WALLET_NOT_CREATED);
        }
        return transferLocks.find(lock -> lock.id().equals(confirmTransferFunds.lockId()))
            .toEither(LOCK_NOT_FOUND)
            .flatMap(transferLock -> {
                TransferFundsWithdrawn transferFundsWithdrawn = new TransferFundsWithdrawn(confirmTransferFunds.toEventMetadata(clock), ownerId, currency, transferLock.amount(), balance, transferLock.id());
                return right(List.of(transferFundsWithdrawn));
            });
    }

    private Either<WalletCommandError, List<WalletEvent>> handleAbortTransfer(AbortTransferFunds abortTransferFunds, Clock clock) {
        //more validation here
        if (isEmpty()) {
            return left(WALLET_NOT_CREATED);
        } else if (transferLocks.exists(lock -> lock.id().equals(abortTransferFunds.lockId()))) {
            TransferFundsAborted transferFundsAborted = new TransferFundsAborted(abortTransferFunds.toEventMetadata(clock), ownerId, currency, abortTransferFunds.lockId(), abortTransferFunds.reason());
            return right(List.of(transferFundsAborted));
        } else {
            return left(LOCK_NOT_FOUND);
        }
    }

    private Either<WalletCommandError, List<WalletEvent>> handleTransferFunds(TransferFunds transferFunds, Clock clock) {
        //more validation here
        if (isEmpty()) {
            return left(WALLET_NOT_CREATED);
        } else if (balance.lessThan(transferFunds.amount())) {
            return left(NOT_SUFFICIENT_FUNDS);
        } else {
            Amount balanceAfter = balance.subtract(transferFunds.amount());
            TransferFundsLocked transferFundsLocked = new TransferFundsLocked(transferFunds.toEventMetadata(clock), ownerId, currency, transferFunds.amount(), balanceAfter, transferFunds.destination(), LockId.of());
            return right(List.of(transferFundsLocked));
        }
    }

    private Either<WalletCommandError, List<WalletEvent>> handleCreation(CreateWallet createWallet, Clock clock) {
        if (!isSupported(createWallet.currency())) {
            return left(CURRENCY_NOT_SUPPORTED);
        } else if (!isEmpty()) {
            return left(WALLET_ALREADY_CREATED);
        } else {
            return right(List.of(new WalletCreated(createWallet.toEventMetadata(clock), createWallet.ownerId(), createWallet.currency(), INITIAL_AMOUNT)));
        }
    }

    private Either<WalletCommandError, List<WalletEvent>> handleWithdraw(WithdrawFunds withdrawFunds, Clock clock) {
        if (isEmpty()) {
            return left(WALLET_NOT_CREATED);
        } else if (!currency.equals(withdrawFunds.currency())) {
            return left(WRONG_CURRENCY);
        } else if (!ownerId.equals(withdrawFunds.ownerId())) {
            return left(NOT_AUTHORIZED);
        } else if (balance.lessThan(withdrawFunds.amount())) {
            return left(NOT_SUFFICIENT_FUNDS);
        } else {
            Amount balanceAfter = balance.subtract(withdrawFunds.amount());
            FundsWithdrawn fundsWithdrawn = new FundsWithdrawn(withdrawFunds.toEventMetadata(clock), ownerId, currency, withdrawFunds.amount(), balanceAfter);
            return right(List.of(fundsWithdrawn));
        }
    }

    private Either<WalletCommandError, List<WalletEvent>> handleDeposit(DepositFunds depositFunds, Clock clock) {
        if (isEmpty()) {
            return left(WALLET_NOT_CREATED);
        } else if (!currency.equals(depositFunds.currency())) {
            return left(WRONG_CURRENCY);
        } else if (!ownerId.equals(depositFunds.ownerId())) {
            return left(NOT_AUTHORIZED);
        } else {
            FundsDeposited fundsDeposited = new FundsDeposited(depositFunds.toEventMetadata(clock), ownerId, currency,
                depositFunds.amount(), depositFunds.amount().add(balance));
            return right(List.of(fundsDeposited));
        }
    }

    public Wallet apply(WalletEvent event) {
        return switch (event) {
            case WalletCreated walletCreated -> create(walletCreated);
            case FundsDeposited fundsDeposited -> applyDeposit(fundsDeposited);
            case FundsWithdrawn fundsWithdrawn -> applyWithdraw(fundsWithdrawn);
            case TransferFundsLocked transferFundsLocked -> applyLocked(transferFundsLocked);
            case TransferFundsAborted transferFundsAborted -> applyAborted(transferFundsAborted);
            case TransferFundsWithdrawn transferFundsWithdrawn -> applyTransferWithdrawn(transferFundsWithdrawn);
            case TransferFundsDeposited transferFundsDeposited -> applyTransferDeposit(transferFundsDeposited);
        };
    }

    private Wallet applyTransferDeposit(TransferFundsDeposited transferFundsDeposited) {
        Amount updatedBalance = balance.add(transferFundsDeposited.transferAmount());
        return new Wallet(id, ownerId, currency, updatedBalance, transferLocks);
    }

    private Wallet applyTransferWithdrawn(TransferFundsWithdrawn transferFundsWithdrawn) {
        List<TransferLock> updatedLocks = transferLocks.reject(lock -> lock.id().equals(transferFundsWithdrawn.lockId()));
        return new Wallet(id, ownerId, currency, balance, updatedLocks);
    }

    private Wallet applyAborted(TransferFundsAborted transferFundsAborted) {
        return transferLocks.find(lock -> lock.id().equals(transferFundsAborted.lockId()))
            .map(abortedLock -> {
                Amount updatedBalance = balance.add(abortedLock.amount());
                List<TransferLock> updatedLocks = transferLocks.reject(lock -> lock.id().equals(transferFundsAborted.lockId()));
                return new Wallet(id, ownerId, currency, updatedBalance, updatedLocks);
            }).getOrElseThrow(() -> new IllegalStateException("Lock " + transferFundsAborted.lockId() + " not found in " + transferLocks));
    }

    private Wallet applyLocked(TransferFundsLocked transferFundsLocked) {
        TransferLock transferLock = new TransferLock(transferFundsLocked.lockId(), transferFundsLocked.destination(), transferFundsLocked.transferAmount());
        return new Wallet(id, ownerId, currency, transferFundsLocked.balanceAfter(), transferLocks.append(transferLock));
    }

    private Wallet create(WalletCreated walletCreated) {
        return new Wallet(walletCreated.walletId(), walletCreated.ownerId(), walletCreated.currency(), walletCreated.amount(), List.empty());
    }

    private Wallet applyWithdraw(FundsWithdrawn fundsWithdrawn) {
        return new Wallet(id, ownerId, currency, fundsWithdrawn.balanceAfter(), transferLocks);
    }

    private Wallet applyDeposit(FundsDeposited fundsDeposited) {
        return new Wallet(id, ownerId, currency, balance.add(fundsDeposited.depositAmount()), transferLocks);
    }
}
