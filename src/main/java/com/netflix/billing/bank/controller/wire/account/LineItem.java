package com.netflix.billing.bank.controller.wire.account;

import java.time.Instant;

// interface for LineItems
public interface LineItem {
    // transactionId for LineItem
    String getTransactionId();
    // transactionDate timestamp
    Instant getTransactionDate();
    // Money associated with LineItem
    Money getMoney();
}
