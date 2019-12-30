package com.netflix.billing.bank.controller.wire.account;

import com.netflix.billing.bank.controller.wire.credit.CreditAmount;
import com.netflix.billing.bank.controller.wire.credit.CreditHistory;
import com.netflix.billing.bank.controller.wire.debit.DebitAmount;
import com.netflix.billing.bank.controller.wire.debit.DebitHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class to maintain credits and debits for each customer
 */
@Component
public class AccountManager {

    // CustomerId to their Accounts map
    private Map<String, Account> customerIdToAccountMap = new ConcurrentHashMap<>();
    // ExecutorService for multi threads
    @Autowired
    AccountExecutorService accountExecutorService;

    // for junits
    public void clear() {
        customerIdToAccountMap.clear();
        customerIdToAccountMap = new ConcurrentHashMap<>();
    }

    /**
     * Posts credit to the account for given customerId
     *
     * @param customerId
     * @param creditAmount
     * @return CustomerBalance
     */
    public CustomerBalance credit(String customerId, CreditAmount creditAmount) {
        if (null == customerId || customerId.isEmpty() || null == creditAmount) {
            throw new Error("Invalid input parameters for credit");
        }

        // credit the customer account
        Account account = customerIdToAccountMap.getOrDefault(customerId, new Account());
        Callable<CustomerBalance> creditTask = () -> {
            account.credit(customerId, creditAmount);
            customerIdToAccountMap.put(customerId, account);
            return account.getBalance();
        };
        // let executorService handle the execution
        return accountExecutorService.execute(creditTask);
    }

    /**
     * Posts debit to given customer's account
     *
     * @param customerId
     * @param debitAmount
     * @return CustomerBalance
     */
    public CustomerBalance debit(String customerId, DebitAmount debitAmount) {
        if (null == customerId || customerId.isEmpty() || null == debitAmount) {
            throw new Error("Invalid input parameters for debit");
        }

        // debit the customer account
        Account account = customerIdToAccountMap.getOrDefault(customerId, new Account());
        Callable<CustomerBalance> debitTask = () -> {
            account.debit(customerId, debitAmount);
            customerIdToAccountMap.put(customerId, account);
            return account.getBalance();
        };
        // let executorService handle the execution
        return accountExecutorService.execute(debitTask);
    }

    /**
     * Returns {@link CustomerBalance} for given customer Id
     *
     * @param customerId
     * @return
     */
    public CustomerBalance getBalance(String customerId) {
        if (null == customerId || customerId.isEmpty()) {
            throw new Error("Invalid customer Id");
        }
        return customerIdToAccountMap.get(customerId).getBalance();
    }

    /**
     * Returns {@link CreditHistory} for given customer Id
     *
     * @param customerId
     * @return CreditHistory
     */
    public CreditHistory getCreditHistory(String customerId) {
        if (null == customerId || customerId.isEmpty()) {
            throw new Error("Invalid customer Id");
        }
        return customerIdToAccountMap.get(customerId).getCreditHistory();
    }

    /**
     * Returns {@link DebitHistory} for given customer Id
     *
     * @param customerId
     * @return DebitHistory
     */
    public DebitHistory getDebitHistory(String customerId) {
        if (null == customerId || customerId.isEmpty()) {
            throw new Error("Invalid customer Id");
        }
        return customerIdToAccountMap.get(customerId).getDebitHistory();
    }
}