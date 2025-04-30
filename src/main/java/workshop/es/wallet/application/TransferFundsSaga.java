package workshop.es.wallet.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.consumer.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import workshop.es.wallet.application.WalletEntityCommandResponse.CommandProcessed;
import workshop.es.wallet.application.WalletEntityCommandResponse.CommandRejected;
import workshop.es.wallet.domain.WalletCommand;
import workshop.es.wallet.domain.WalletCommand.DepositTransferFunds;
import workshop.es.wallet.domain.WalletEvent;
import workshop.es.wallet.domain.WalletEvent.TransferFundsDeposited;
import workshop.es.wallet.domain.WalletEvent.TransferFundsLocked;
import workshop.es.wallet.domain.type.CommandId;

@ComponentId("transfer-funds-saga")
@Consume.FromEventSourcedEntity(WalletEntity.class)
public class TransferFundsSaga extends Consumer {

    private static final Logger log = LoggerFactory.getLogger(TransferFundsSaga.class);
    private final ComponentClient componentClient;

    public TransferFundsSaga(ComponentClient componentClient) {
        this.componentClient = componentClient;
    }

    public Effect onEvent(WalletEvent event) {
        return switch (event) {
            case TransferFundsLocked transferFundsLocked -> {
                log.info("Starting transfer: {}", event);
                var result = depositTransferFunds(transferFundsLocked);
                yield switch (result) {
                    case CommandProcessed __ -> effects().done();
                    case CommandRejected rejected -> {
                        abortTransfer(transferFundsLocked, rejected);
                        //response handling is simplified, aborting transfer can also fail
                        yield effects().done();
                    }
                };
            }
            case TransferFundsDeposited transferFundsDeposited -> {
                confirmTransfer(event, transferFundsDeposited);
                //response handling is simplified, confirming transfer can also fail
                yield effects().done();
            }
            default -> effects().ignore();
        };
    }

    private WalletEntityCommandResponse confirmTransfer(WalletEvent event, TransferFundsDeposited transferFundsDeposited) {
        log.info("Finishing transfer: {}", event);
        var commandId = CommandId.of(transferFundsDeposited.metadata().eventId().value());
        var confirmTransferFunds = new WalletCommand.ConfirmTransferFunds(transferFundsDeposited.source(), commandId, transferFundsDeposited.currency(), transferFundsDeposited.sourceLockId());
        return componentClient.forEventSourcedEntity(transferFundsDeposited.source().value())
            .method(WalletEntity::handleCommand)
            .invoke(confirmTransferFunds);
    }

    private WalletEntityCommandResponse abortTransfer(TransferFundsLocked transferFundsLocked, CommandRejected rejected) {
        log.info("Transfer rejected, aborting transfer: {}", rejected);
        var commandId = CommandId.of(transferFundsLocked.metadata().eventId().value());
        var abortTransferFunds = new WalletCommand.AbortTransferFunds(transferFundsLocked.walletId(), commandId, transferFundsLocked.currency(), transferFundsLocked.lockId(), rejected.error().toString());
        return componentClient.forEventSourcedEntity(transferFundsLocked.destination().value())
            .method(WalletEntity::handleCommand)
            .invoke(abortTransferFunds);
    }

    private WalletEntityCommandResponse depositTransferFunds(TransferFundsLocked transferFundsLocked) {
        var commandId = CommandId.of(transferFundsLocked.metadata().eventId().value());
        var depositTransferFunds = new DepositTransferFunds(transferFundsLocked.destination(), commandId, transferFundsLocked.currency(), transferFundsLocked.transferAmount(), transferFundsLocked.walletId(), transferFundsLocked.lockId());
        var result = componentClient.forEventSourcedEntity(transferFundsLocked.destination().value())
            .method(WalletEntity::handleCommand)
            .invoke(depositTransferFunds);
        return result;
    }
}
