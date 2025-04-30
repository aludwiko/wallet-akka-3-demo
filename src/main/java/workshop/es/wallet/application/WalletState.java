package workshop.es.wallet.application;

import workshop.es.wallet.domain.Wallet;
import workshop.es.wallet.domain.WalletCommand;
import workshop.es.wallet.domain.WalletEvent;

import java.io.Serializable;

public record WalletState(Wallet wallet, CommandIdCache commandIdCache) implements Serializable {

    public static WalletState empty() {
        return new WalletState(Wallet.EMPTY, CommandIdCache.empty());
    }

    public boolean isEmpty() {
        return wallet.equals(Wallet.EMPTY);
    }

    public WalletState apply(WalletEvent event) {
        commandIdCache.put(event.metadata().commandId());
        return new WalletState(wallet.apply(event), commandIdCache);
    }

    public boolean isDuplicate(WalletCommand command) {
        return commandIdCache().contains(command.commandId());
    }

    @Override
    public String toString() {
        return wallet.toString();
    }
}
