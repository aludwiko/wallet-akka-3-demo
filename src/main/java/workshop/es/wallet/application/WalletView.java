package workshop.es.wallet.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.annotations.Query;
import akka.javasdk.view.TableUpdater;
import akka.javasdk.view.View;
import workshop.es.wallet.domain.WalletEvent;
import workshop.es.wallet.domain.WalletEvent.FundsDeposited;
import workshop.es.wallet.domain.WalletEvent.FundsWithdrawn;
import workshop.es.wallet.domain.WalletEvent.TransferFundsAborted;
import workshop.es.wallet.domain.WalletEvent.TransferFundsDeposited;
import workshop.es.wallet.domain.WalletEvent.TransferFundsLocked;
import workshop.es.wallet.domain.WalletEvent.TransferFundsWithdrawn;
import workshop.es.wallet.domain.WalletEvent.WalletCreated;
import workshop.es.wallet.domain.type.Amount;

import java.util.List;

@ComponentId("wallet-view")
public class WalletView extends View {

    public record WalletEntry(
        String id,
        String ownerId,
        String currency,
        double balance
    ) {
        public WalletEntry add(Amount amount) {
            return new WalletEntry(id, ownerId, currency, Amount.of(balance).add(amount).value().doubleValue());
        }

        public WalletEntry withBalance(Amount amount) {
            return new WalletEntry(id, ownerId, currency, amount.value().doubleValue());
        }
    }

    public record WalletEntries(List<WalletEntry> entries){}

    @Consume.FromEventSourcedEntity(WalletEntity.class)
    public static class WalletsUpdater extends TableUpdater<WalletEntry> {
        public Effect<WalletEntry> onChange(WalletEvent walletEvent) {
            return switch (walletEvent) {
                case WalletCreated walletCreated -> effects().updateRow(new WalletEntry(
                    walletCreated.walletId().value(),
                    walletCreated.ownerId().value(),
                    walletCreated.currency().value(),
                    walletCreated.amount().value().doubleValue()));
                case FundsDeposited fundsDeposited ->
                    effects().updateRow(rowState().withBalance(fundsDeposited.balanceAfter()));
                case FundsWithdrawn fundsWithdrawn ->
                    effects().updateRow(rowState().withBalance(fundsWithdrawn.balanceAfter()));
                case TransferFundsAborted transferFundsAborted -> effects().ignore();
                case TransferFundsDeposited transferFundsDeposited ->
                    effects().updateRow(rowState().withBalance(transferFundsDeposited.balanceAfter()));
                case TransferFundsLocked transferFundsLocked ->
                    effects().updateRow(rowState().withBalance(transferFundsLocked.balanceAfter()));
                case TransferFundsWithdrawn transferFundsWithdrawn ->
                    effects().updateRow(rowState().withBalance(transferFundsWithdrawn.balanceAfter()));
            };
        }
    }

    @Query("select * as entries from wallets")
    public QueryEffect<WalletEntries> getAllWallets(){
        return queryResult();
    }

    @Query("select * as entries from wallets where ownerId = :ownerId")
    public QueryEffect<WalletEntries> getByOwnerId(String ownerId){
        return queryResult();
    }

}
