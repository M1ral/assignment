package com.netflix.billing.bank.controller.wire.debit;

import com.netflix.billing.bank.controller.wire.Money;

import java.time.Instant;

/**
 * Wire objects representing debit transactions to the customer account. The amount represented here is by how much
 * a customer's balance will go down, once the debit transaction is applied.
 */
public class DebitAmount {
    private String invoiceId; //Id denoting the receipt for the charge. Should be unique for a given customer.
    private Money money;

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public Money getMoney() {
        return money;
    }

    public void setMoney(Money money) {
        this.money = money;
    }

    public DebitLineItem toDebitLineItem() {
        return new DebitLineItem(this.invoiceId, this.money, Instant.now());
    }
}
