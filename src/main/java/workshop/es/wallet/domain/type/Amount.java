package workshop.es.wallet.domain.type;

import java.math.BigDecimal;

public record Amount(BigDecimal value)  {

    public static Amount of(int value) {
        return new Amount(BigDecimal.valueOf(value));
    }

    public static Amount of(double value) {
        return new Amount(BigDecimal.valueOf(value));
    }

    public static Amount of(String value) {
        return new Amount(new BigDecimal(value));
    }

    public Amount add(Amount amount) {
        return new Amount(value.add(amount.value));
    }

    public Amount subtract(Amount subtrahend) {
        return new Amount(value.subtract(subtrahend.value));
    }

    public boolean lessThan(Amount amount) {
        return value.compareTo(amount.value) < 0;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
