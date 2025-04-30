package workshop.es.wallet.domain;

/**
 * this should be split into domain errors and application/entity errors
 */
public enum WalletCommandError {
    WALLET_NOT_CREATED, CURRENCY_NOT_SUPPORTED, WRONG_CURRENCY, NOT_AUTHORIZED, WALLET_ALREADY_CREATED, NOT_SUFFICIENT_FUNDS, DUPLICATED_COMMAND, LOCK_NOT_FOUND;
}
