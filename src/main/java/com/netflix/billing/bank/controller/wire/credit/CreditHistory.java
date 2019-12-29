package com.netflix.billing.bank.controller.wire.credit;

import java.util.List;

public class CreditHistory {

    private List<CreditLineItem> creditHistory;

    public List<CreditLineItem> getCreditHistory() {
        return creditHistory;
    }

    public void setCreditHistory(List<CreditLineItem> creditHistory) {
        this.creditHistory = creditHistory;
    }
}
