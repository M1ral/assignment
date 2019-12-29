package com.netflix.billing.bank.controller.wire.credit;

import com.netflix.billing.bank.controller.wire.Money;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class CreditLineItem {

    private String transactionId;
    private CreditType creditType;
    private Money amount;
    private Instant transactionDate;
    private List<String> invoiceIdList; // list of invoieIds credit is debited to

    public CreditLineItem(String transactionId, CreditType creditType, Money amount, Instant transactionDate) {
        this.transactionId = transactionId;
        this.creditType = creditType;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.invoiceIdList = new ArrayList<>();
    }

    public void recordDebit(String invoiceId) {
        if (null == invoiceId || invoiceId.isEmpty()) {
            return;
        }
        invoiceIdList.add(invoiceId);
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public CreditType getCreditType() {
        return creditType;
    }

    public void setCreditType(CreditType creditType) {
        this.creditType = creditType;
    }

    public Money getAmount() {
        return amount;
    }

    public void setAmount(Money amount) {
        this.amount = amount;
    }

    public Instant getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Instant transactionDate) {
        this.transactionDate = transactionDate;
    }

    public List<String> getInvoiceIdList() {
        return invoiceIdList;
    }

    public void setInvoiceIdList(List<String> invoiceIdList) {
        this.invoiceIdList = invoiceIdList;
    }

}
