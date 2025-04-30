package workshop.es.wallet.api;

import akka.http.javadsl.model.HttpResponse;
import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.annotations.http.Post;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.http.HttpResponses;
import workshop.es.wallet.application.WalletEntity;
import workshop.es.wallet.application.WalletEntity.WalletResponse;
import workshop.es.wallet.application.WalletEntityCommandResponse;
import workshop.es.wallet.application.WalletEntityCommandResponse.CommandProcessed;
import workshop.es.wallet.application.WalletEntityCommandResponse.CommandRejected;
import workshop.es.wallet.application.WalletView;
import workshop.es.wallet.application.WalletView.WalletEntry;
import workshop.es.wallet.domain.WalletCommand.CreateWallet;
import workshop.es.wallet.domain.WalletCommand.DepositFunds;
import workshop.es.wallet.domain.WalletCommand.TransferFunds;
import workshop.es.wallet.domain.WalletCommand.WithdrawFunds;
import workshop.es.wallet.domain.type.Amount;
import workshop.es.wallet.domain.type.CommandId;
import workshop.es.wallet.domain.type.Currency;
import workshop.es.wallet.domain.type.OwnerId;
import workshop.es.wallet.domain.type.WalletId;

import java.util.List;

/**
 * This is a simple Akka Endpoint that returns "Hello World!".
 * Locally, you can access it by running `curl http://localhost:9000/hello`.
 */
// Opened up for access from the public internet to make the service easy to try out.
// For actual services meant for production this must be carefully considered, and often set more limited
@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
@HttpEndpoint("/wallets")
public class WalletEndpoint {

    private final ComponentClient componentClient;

    public WalletEndpoint(ComponentClient componentClient) {
        this.componentClient = componentClient;
    }

    record CreateRequest(String ownerId, String currency) {
        public CreateWallet toCommand(String walletId) {
            //FIXME commandId should not be generated in the controller, it should be part of the request
            CommandId commandId = CommandId.of();
            return new CreateWallet(WalletId.of(walletId), commandId, OwnerId.of(ownerId), Currency.of(currency));
        }
    }

    record DepositRequest(String ownerId, String currency, String amount) {
        public DepositFunds toCommand(String walletId) {
            //FIXME commandId should not be generated in the controller, it should be part of the request
            CommandId commandId = CommandId.of();
            return new DepositFunds(WalletId.of(walletId), commandId, OwnerId.of(ownerId), Currency.of(currency), Amount.of(amount));
        }
    }

    record WithdrawRequest(String ownerId, String currency, String amount) {
        public WithdrawFunds toCommand(String walletId) {
            //FIXME commandId should not be generated in the controller, it should be part of the request
            CommandId commandId = CommandId.of();
            return new WithdrawFunds(WalletId.of(walletId), commandId, OwnerId.of(ownerId), Currency.of(currency), Amount.of(amount));
        }
    }

    record TransferRequest(String ownerId, String currency, String amount, String destinationWalletId) {
        public TransferFunds toCommand(String walletId) {
            //FIXME commandId should not be generated in the controller, it should be part of the request
            CommandId commandId = CommandId.of();
            return new TransferFunds(WalletId.of(walletId), commandId, OwnerId.of(ownerId), Currency.of(currency), Amount.of(amount), WalletId.of(destinationWalletId));
        }
    }

    @Post("/{walletId}/create")
    public HttpResponse create(String walletId, CreateRequest request) {
        var entityResponse = componentClient.forEventSourcedEntity(walletId)
            .method(WalletEntity::handleCommand)
            .invoke(request.toCommand(walletId));
        return transformResponse(entityResponse);
    }

    @Post("/{walletId}/deposit")
    public HttpResponse deposit(String walletId, DepositRequest request) {
        var entityResponse = componentClient.forEventSourcedEntity(walletId)
            .method(WalletEntity::handleCommand)
            .invoke(request.toCommand(walletId));
        return transformResponse(entityResponse);
    }

    @Post("/{walletId}/withdraw")
    public HttpResponse withdraw(String walletId, WithdrawRequest request) {
        var entityResponse = componentClient.forEventSourcedEntity(walletId)
            .method(WalletEntity::handleCommand)
            .invoke(request.toCommand(walletId));
        return transformResponse(entityResponse);
    }

    @Post("/{walletId}/transfer")
    public HttpResponse transfer(String walletId, TransferRequest request) {
        var entityResponse = componentClient.forEventSourcedEntity(walletId)
            .method(WalletEntity::handleCommand)
            .invoke(request.toCommand(walletId));
        return transformResponse(entityResponse);
    }

    private HttpResponse transformResponse(WalletEntityCommandResponse entityResponse) {
        return switch (entityResponse) {
            case CommandProcessed __ -> HttpResponses.ok();
            case CommandRejected commandRejected -> HttpResponses.badRequest("Rejected: " + commandRejected.error());
        };
    }

    @Get("/{walletId}")
    public WalletResponse get(String walletId) {
        return componentClient.forEventSourcedEntity(walletId)
            .method(WalletEntity::get)
            .invoke();
    }

    @Get
    public List<WalletEntry> getAll() {
        return componentClient.forView()
            .method(WalletView::getAllWallets)
            .invoke()
            .entries();
    }
}
