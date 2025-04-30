package workshop.es.wallet.application;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import workshop.es.wallet.domain.WalletCommandError;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = WalletEntityCommandResponse.CommandProcessed.class, name = "processed"),
    @JsonSubTypes.Type(value = WalletEntityCommandResponse.CommandRejected.class, name = "rejected")
})
public sealed interface WalletEntityCommandResponse {

    final class CommandProcessed implements WalletEntityCommandResponse {
    }

    record CommandRejected(WalletCommandError error) implements WalletEntityCommandResponse {
    }
}
