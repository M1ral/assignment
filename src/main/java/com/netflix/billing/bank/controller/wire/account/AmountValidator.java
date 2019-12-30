package com.netflix.billing.bank.controller.wire.account;

import com.netflix.billing.bank.controller.wire.credit.CreditAmount;
import com.netflix.billing.bank.controller.wire.debit.DebitAmount;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AmountValidator {

    /**
     * Validate {@link CreditAmount}
     * @param amount
     * @return boolean
     */
    public boolean validateCredit(CreditAmount amount) {
        if (null == amount || null == amount.getTransactionId() || amount.getTransactionId().isEmpty()) {
            return false;
        }
        if (null == amount.getCreditType() || null == amount.getMoney() ||
                null == amount.getMoney().getAmount() || amount.getMoney().getAmount().compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }
        return true;
    }

    /**
     * Validate {@link DebitAmount}
     * @param amount
     * @return boolean
     */
    public boolean validateDebit(DebitAmount amount) {
        if (null == amount || null == amount.getInvoiceId() || amount.getInvoiceId().isEmpty()) {
            return false;
        }
        if (null == amount.getMoney() ||
                null == amount.getMoney().getAmount() || amount.getMoney().getAmount().compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }
        return true;
    }
}
