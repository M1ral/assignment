package com.netflix.billing.bank.controller.wire.account;

import com.netflix.billing.bank.controller.wire.credit.CreditAmount;
import com.netflix.billing.bank.controller.wire.credit.CreditHistory;
import com.netflix.billing.bank.controller.wire.credit.CreditLineItem;
import com.netflix.billing.bank.controller.wire.credit.CreditType;
import com.netflix.billing.bank.controller.wire.debit.DebitAmount;
import com.netflix.billing.bank.controller.wire.debit.DebitHistory;
import com.netflix.billing.bank.controller.wire.debit.DebitLineItem;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

// Customer Bank Account
public class Account {

    // credits
    private Map<CreditType, List<CreditLineItem>> creditsMap;
    // debits
    private List<DebitLineItem> debitsList;
    // credit history
    private CreditHistory creditHistory;
    // debit history
    private DebitHistory debitHistory;
    // total credit amount
    private volatile BigDecimal totalCreditAmount;
    private Map<CreditType, Set<String>> processedTransactions; // for dedupe credits
    private Set<String> processedInvoices; // for dedupe debits

    // new Account constructor
    public Account() {
        creditsMap = new ConcurrentHashMap<>();
        debitsList = Collections.synchronizedList(new ArrayList<>());
        creditHistory = new CreditHistory();
        debitHistory = new DebitHistory();
        totalCreditAmount = new BigDecimal(0);
        processedTransactions = new ConcurrentHashMap<>();
        processedInvoices = Collections.synchronizedSet(new HashSet<>());
    }

    /**
     * Returns current snapshot of {@link CustomerBalance}
     * @return CustomerBalance
     */
    public CustomerBalance getBalance() {
        CustomerBalance balance = new CustomerBalance();
        // build CustomerBalance
        for (CreditType creditType : CreditType.values()) {
            if (creditsMap.containsKey(creditType)) {
                balance.addAll(creditType, creditsMap.get(creditType)
                        .stream()
                        .map(cb -> cb.getMoney())
                        .collect(Collectors.toList()));
            }
        }

        return balance;
    }

    /**
     * Record credit to customer account
     * @param creditAmount
     */
    public synchronized void credit(CreditAmount creditAmount) {
        if (null == creditAmount) {
            return;
        }

        // dedupe check
        if (processedTransactions.containsKey(creditAmount.getCreditType()) &&
                processedTransactions.get(creditAmount.getCreditType()).contains(creditAmount.getTransactionId())) {
            return;
        }

        CreditLineItem creditLineItem = creditAmount.toCreditLineItem();
        CreditType creditType = creditLineItem.getCreditType();
        List<CreditLineItem> creditLineItems = creditsMap.getOrDefault(creditType, new ArrayList<>());

        // add CreditLineItem
        creditLineItems.add(creditLineItem);
        creditsMap.put(creditType, creditLineItems);
        // update total credit amaunt
        // totalCreditAmount += creditLineItem.getMoney().getAmount();
        totalCreditAmount = totalCreditAmount.add(creditLineItem.getMoney().getAmount());

        // add to the history
        this.getCreditHistory().add(creditLineItem);
        // mark processed
        Set<String> transactionIds = processedTransactions.getOrDefault(creditType, new HashSet<>());
        transactionIds.add(creditLineItem.getTransactionId());
        processedTransactions.put(creditType, transactionIds);
    }

    /**
     * Record debit to customer account
     * @param debitAmount
     */
    public synchronized void debit(DebitAmount debitAmount) throws Exception {
        if (null == debitAmount) {
            return;
        }
        // dedupe debit
        if (processedInvoices.contains(debitAmount.getInvoiceId())) {
            return;
        }

        BigDecimal debitAmountValue = debitAmount.getMoney().getAmount();
        if (debitAmountValue.compareTo(totalCreditAmount) > 0) { // Error scenario
            throw new Exception("Insufficient balance");
        }

        outer : for (CreditType creditType : CreditType.values()) {
            if (!creditsMap.containsKey(creditType)) {
                continue;
            }

            for (Iterator<CreditLineItem> it = creditsMap.get(creditType).iterator(); it.hasNext();) {
                CreditLineItem creditLineItem = it.next();
                BigDecimal currentCreditAmount = creditLineItem.getMoney().getAmount();
                creditLineItem.getInvoiceIdList().add(debitAmount.getInvoiceId()); // invoiceId

                // current credit amount <= debit amount, consume whole credit
                int cmp = currentCreditAmount.compareTo(debitAmountValue);
                if (cmp <= 0) {
                    debitAmountValue = debitAmountValue.subtract(currentCreditAmount);
                    totalCreditAmount = totalCreditAmount.subtract(currentCreditAmount);
                    it.remove(); // remove current CreditLineItem
                } else {
                    // current credit amount > debit amount, consume partial credit
                    currentCreditAmount = currentCreditAmount.subtract(debitAmountValue);
                    totalCreditAmount = totalCreditAmount.subtract(debitAmountValue);
                    debitAmountValue = new BigDecimal(0);
                    creditLineItem.getMoney().setAmount(currentCreditAmount);
                }

                DebitLineItem debitLineItem = debitAmount.toDebitLineItem();
                debitLineItem.setCreditType(creditType); // creditType
                debitLineItem.setTransactionId(creditLineItem.getTransactionId()); // transactionId
                debitLineItem.setTransactionDate(Instant.now()); // instant
                // add to debits list
                debitsList.add(debitLineItem);
                // add to the history
                this.getDebitHistory().add(debitLineItem);

                if (debitAmountValue.compareTo(BigDecimal.ZERO) == 0) {  // consumed credits for given DebitLineItem
                    break outer; // break outer for loop
                }
            }
        }
        processedInvoices.add(debitAmount.getInvoiceId());
    }

    public CreditHistory getCreditHistory() {
        return creditHistory;
    }

    public DebitHistory getDebitHistory() {
        return debitHistory;
    }
}
