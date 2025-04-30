package workshop.es.wallet.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;
import workshop.es.base.domain.Clock;
import workshop.es.wallet.domain.Wallet;
import workshop.es.wallet.domain.WalletCommand;
import workshop.es.wallet.domain.WalletEvent;

@ComponentId("wallet")
public class WalletEntity extends EventSourcedEntity<WalletState, WalletEvent> {

    private final Clock clock;

    public WalletEntity(Clock clock) {
        this.clock = clock;
    }

    @Override
    public WalletState emptyState() {
        return WalletState.empty();
    }

    public Effect<WalletEntityCommandResponse> handleCommand(WalletCommand command) {
        return currentState().wallet().process(command, clock)
            .fold(
                error -> effects()
                    .reply(new WalletEntityCommandResponse.CommandRejected(error)),
                events -> effects()
                    .persistAll(events.toJavaList())
                    .thenReply(s -> new WalletEntityCommandResponse.CommandProcessed())
            );
    }

    public Effect<WalletResponse> get() {
        if (currentState().isEmpty()){
            return effects().error("Wallet not found");
        }
        return effects().reply(WalletResponse.from(currentState().wallet()));
    }

    @Override
    public WalletState applyEvent(WalletEvent event) {
        return currentState().apply(event);
    }

    public record WalletResponse(String id, String ownerId, String currency, String balance) {
        public static WalletResponse from(Wallet wallet) {
            return new WalletResponse(wallet.id().value(), wallet.ownerId().value(), wallet.currency().value(), wallet.balance().value().toString());
        }
    }
}
