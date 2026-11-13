package useraccounts;

/** Lifecycle: PENDING -> ACTIVE <-> SUSPENDED; any non-deleted state -> DELETED (terminal). */
public enum AccountStatus { PENDING, ACTIVE, SUSPENDED, DELETED }
