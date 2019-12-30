package com.netflix.billing.bank.controller.wire.account;

import com.netflix.billing.bank.controller.wire.credit.CreditAmount;
import com.netflix.billing.bank.controller.wire.credit.CreditHistory;
import com.netflix.billing.bank.controller.wire.debit.DebitAmount;
import com.netflix.billing.bank.controller.wire.debit.DebitHistory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class to maintain credits and debits for each customer
 */
@Component
public class AccountManager {

    // CustomerId to their Accounts map
    private Map<String, Account> customerIdToAccountMap = new ConcurrentHashMap<>();

    /**
     * Adds credit to the account for given customerId
     *
     * @param customerId
     * @param creditAmount
     * @return CustomerBalance
     */
    public CustomerBalance addCredit(String customerId, CreditAmount creditAmount) {
        if (null == customerId || customerId.isEmpty() || null == creditAmount) {
            return null;
        }

        // credit the customer account
        Account account = customerIdToAccountMap.getOrDefault(customerId, new Account());
        account.credit(creditAmount);

        // add account to map
        customerIdToAccountMap.put(customerId, account);
        return account.getBalance();
    }

    /**
     * Posts debit to given customer's account
     *
     * @param customerId
     * @param debitAmount
     * @return CustomerBalance
     */
    public CustomerBalance addDebit(String customerId, DebitAmount debitAmount) throws Exception {
        if (null == customerId || customerId.isEmpty() || null == debitAmount) {
            return null;
        }

        // credit the customer account
        Account account = customerIdToAccountMap.getOrDefault(customerId, new Account());
        account.debit(debitAmount);

        // add account to map
        customerIdToAccountMap.put(customerId, account);
        return account.getBalance();
    }

    /**
     * Returns {@link CustomerBalance} for given customer Id
     *
     * @param customerId
     * @return
     */
    public CustomerBalance getBalance(String customerId) {
        if (null == customerId || customerId.isEmpty()) {
            // throw new Exception("Invalid customer Id");
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
            return null;
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
            return null;
        }
        return customerIdToAccountMap.get(customerId).getDebitHistory();
    }
}
