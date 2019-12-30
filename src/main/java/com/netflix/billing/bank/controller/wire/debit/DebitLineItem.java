package com.netflix.billing.bank.controller.wire.debit;

import com.netflix.billing.bank.controller.wire.account.LineItem;
import com.netflix.billing.bank.controller.wire.credit.CreditType;
import com.netflix.billing.bank.controller.wire.account.Money;

import java.time.Instant;

/**
 * Represents a single debit transaction that was applied to the customer's account.
 */
public class DebitLineItem implements LineItem {
    private String invoiceId;
    private Money money;
    private String transactionId;  // Credit transactionId it was charged against.
    private CreditType creditType; // Credit type it was charged against.
    private Instant transactionDate; //The time in UTC when the debit was applied to the account.

    public DebitLineItem(String invoiceId, Money amount, Instant transactionDate) {
        this.invoiceId = invoiceId;
        this.money = amount;
        this.transactionDate = transactionDate;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public Money getMoney() {
        return money;
    }

    public void setAmount(Money amount) {
        this.money = amount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Instant getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Instant transactionDate) {
        this.transactionDate = transactionDate;
    }

    public CreditType getCreditType() {
        return creditType;
    }

    public void setCreditType(CreditType creditType) {
        this.creditType = creditType;
    }
}

