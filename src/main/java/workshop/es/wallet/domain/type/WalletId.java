package workshop.es.wallet.domain.type;

public record WalletId(String value) {
    @Override
    public String toString() {
        return value;
    }

    public static WalletId of(String value) {
        return new WalletId(value);
    }
}
