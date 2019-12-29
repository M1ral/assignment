package com.netflix.billing.bank.controller.wire;

import com.netflix.billing.bank.controller.wire.credit.CreditHistory;
import com.netflix.billing.bank.controller.wire.credit.CreditLineItem;
import com.netflix.billing.bank.controller.wire.debit.DebitHistory;
import com.netflix.billing.bank.controller.wire.debit.DebitLineItem;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class to maintain credits and debits for each customer
 */
@Component
public class BankAccountsManager {

    private Map<String, CustomerBalance> custIdToBalanceMap = new ConcurrentHashMap<>();
    private Map<String, CreditHistory> custIdToCreditHistoryMap = new HashMap<>();
    private Map<String, DebitHistory> custIdToDebitHistoryMap = new HashMap<>();

    /**
     * Returns CustomerBalance after posting given credit to customer's account
     * @param customerId
     * @param creditLineItem
     * @return
     */
    public CustomerBalance addCredit(String customerId, CreditLineItem creditLineItem) {
        if (null == customerId || customerId.isEmpty() || null == creditLineItem) {
            return null;
        }

        // get customer balance for customer Id
        CustomerBalance customerBalance = custIdToBalanceMap.getOrDefault(customerId, new CustomerBalance());
        customerBalance.recordCredit(creditLineItem);

        custIdToBalanceMap.put(customerId, customerBalance);
        return customerBalance;
    }

    /**
     * Returns CustomerBalance after posting given debit to customer's account
     * @param customerId
     * @param debitLineItem
     * @return
     */
    public CustomerBalance addDebit(String customerId, DebitLineItem debitLineItem) {
        return null;
    }

    /**
     * Returns {@link CustomerBalance} for given customer Id
     * @param customerId
     * @return
     */
    public CustomerBalance getBalance(String customerId) {
        if (null == customerId || customerId.isEmpty()) {
            // throw new Exception("Invalid customer Id");
        }
        return custIdToBalanceMap.get(customerId);
    }

    /**
     * Returns {@link CreditHistory} for given customer Id
     * @param customerId
     * @return CreditHistory
     */
    public CreditHistory getCreditHistory(String customerId) {
        if (null == customerId || customerId.isEmpty()) {
            return null;
        }
        return this.custIdToCreditHistoryMap.get(customerId);
    }

    /**
     * Returns {@link DebitHistory} for given customer Id
     * @param customerId
     * @return DebitHistory
     */
    public DebitHistory getDebitHistory(String customerId) {
        if (null == customerId || customerId.isEmpty()) {
            return null;
        }
        return this.custIdToDebitHistoryMap.get(customerId);
    }
}
