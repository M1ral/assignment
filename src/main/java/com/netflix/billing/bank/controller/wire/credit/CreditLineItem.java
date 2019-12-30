package com.netflix.billing.bank.controller.wire.credit;

import com.netflix.billing.bank.controller.wire.account.LineItem;
import com.netflix.billing.bank.controller.wire.account.Money;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class CreditLineItem implements LineItem {

    private String transactionId;
    private CreditType creditType;
    private Money money;
    private Instant transactionDate;
    private List<String> invoiceIdList; // list of invoieIds credit is debited to

    public CreditLineItem(String transactionId, CreditType creditType, Money money, Instant transactionDate) {
        this.transactionId = transactionId;
        this.creditType = creditType;
        this.money = money;
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

    public Money getMoney() {
        return money;
    }

    public void setMoney(Money money) {
        this.money = money;
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
