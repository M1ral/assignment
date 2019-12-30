package com.netflix.billing.bank.controller.wire.credit;

import java.util.ArrayList;
import java.util.List;

public class CreditHistory {

    public CreditHistory() {
        this.creditHistory = new ArrayList<>();
    }

    public void add(CreditLineItem creditLineItem) {
        this.creditHistory.add(creditLineItem);
    }

    private volatile List<CreditLineItem> creditHistory;

    public List<CreditLineItem> getCreditHistory() {
        return creditHistory;
    }

    public void setCreditHistory(List<CreditLineItem> creditHistory) {
        this.creditHistory = creditHistory;
    }
}
