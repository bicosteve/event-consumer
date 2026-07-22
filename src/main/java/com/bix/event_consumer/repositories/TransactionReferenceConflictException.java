package com.bix.event_consumer.repositories;

public class TransactionReferenceConflictException extends IllegalStateException {
    public TransactionReferenceConflictException(String reference) {
        super("Transaction reference conflicts with existing immutable fields: " + reference);
    }
}
