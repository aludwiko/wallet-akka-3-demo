package workshop.es.wallet.domain.type;

public record Currency(String value) {

    public static Currency of(String value) {
        return new Currency(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
