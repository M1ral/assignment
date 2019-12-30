package com.netflix.billing.bank.controller.wire.debit;

import java.util.ArrayList;
import java.util.List;

/**
 * List of all the debit transactions applied to the customer's account.
 */
public class DebitHistory {
    private List<DebitLineItem> debits;

    public void add(DebitLineItem debitLineItem) {
        this.debits.add(debitLineItem);
    }

    public DebitHistory() {
        this.debits = new ArrayList<>();
    }

    public List<DebitLineItem> getDebits() {
        return debits;
    }

    public void setDebits(List<DebitLineItem> debits) {
        this.debits = debits;
    }
}
