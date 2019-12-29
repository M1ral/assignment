package com.netflix.billing.bank.controller.wire;

import com.netflix.billing.bank.controller.wire.credit.CreditLineItem;
import com.netflix.billing.bank.controller.wire.credit.CreditType;
import com.netflix.billing.bank.controller.wire.debit.DebitLineItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents the sum total of all the credits a customer has left after deducting all the debit transactions
 * from the their account.
 */
public class CustomerBalance {
    public CustomerBalance() {
        this.balanceAmounts = new ConcurrentHashMap<>();
    }

    private Map<CreditType, List<Money>> balanceAmounts;

    public Map<CreditType, List<Money>> getBalanceAmounts() {
        return balanceAmounts;
    }

    public void setBalanceAmounts(Map<CreditType, List<Money>> balanceAmounts) {
        this.balanceAmounts = balanceAmounts;
    }

    private boolean addBalance(CreditLineItem creditLineItem) {
        if (null == creditLineItem || null == creditLineItem.getCreditType()) {
            return false;
        }

        CreditType creditType = creditLineItem.getCreditType();
        Money amount = creditLineItem.getAmount();

        List<Money> amounts = balanceAmounts.getOrDefault(creditType, new ArrayList<Money>());
        amounts.add(amount);
        balanceAmounts.put(creditType, amounts);
        return true;
    }

    public boolean deductBalance() {
        return true;
    }


    // TODO - move this methods to a separate util class

    public boolean recordCredit(CreditLineItem creditLineItem) {
        if (null == creditLineItem) {
            return false;
        }
        return addBalance(creditLineItem);
    }

    public List<String> recordDebit(DebitLineItem debitLineItem) {
        return null;
    }
}

