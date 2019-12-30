package com.netflix.billing.bank.controller.wire.account;

import com.netflix.billing.bank.controller.wire.credit.CreditLineItem;
import com.netflix.billing.bank.controller.wire.credit.CreditType;
import com.netflix.billing.bank.controller.wire.debit.DebitLineItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents the sum total of all the credits a customer has left after deducting all the debit transactions
 * from the their account.
 */
public class CustomerBalance {
    private Map<CreditType, List<Money>> balanceAmounts;

    public CustomerBalance() {
        this.balanceAmounts = new ConcurrentHashMap<>();
    }

    public Map<CreditType, List<Money>> getBalanceAmounts() {
        return balanceAmounts;
    }

    public void setBalanceAmounts(Map<CreditType, List<Money>> balanceAmounts) {
        this.balanceAmounts = balanceAmounts;
    }

    /**
     * Add given money for creditType
     * @param creditType
     * @param money
     */
    public void add(CreditType creditType, Money money) {
        addAll(creditType, Arrays.asList(money));
    }

    /**
     * Add given list of money for credit type
     * @param creditType
     * @param moneyList
     */
    public void addAll(CreditType creditType, List<Money> moneyList) {
        List<Money> list = balanceAmounts.getOrDefault(creditType, new ArrayList<>());
        list.addAll(moneyList);
        balanceAmounts.put(creditType, list);
    }
}

