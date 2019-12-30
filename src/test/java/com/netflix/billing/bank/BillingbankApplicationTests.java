package com.netflix.billing.bank;

import com.netflix.billing.bank.controller.BankController;
import com.netflix.billing.bank.controller.wire.account.AccountManager;
import com.netflix.billing.bank.controller.wire.account.CustomerBalance;
import com.netflix.billing.bank.controller.wire.account.Money;
import com.netflix.billing.bank.controller.wire.credit.CreditAmount;
import com.netflix.billing.bank.controller.wire.credit.CreditType;
import com.netflix.billing.bank.controller.wire.debit.DebitAmount;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.netflix.billing.bank.controller.wire.account.Currency.USD;
import static com.netflix.billing.bank.controller.wire.credit.CreditType.*;
import static junit.framework.TestCase.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BillingbankApplicationTests {

	@Autowired
	public BankController bankController;

	@Autowired
	public AccountManager accountManager;

	@After
	public void afterTest() {
		accountManager.clear();
	}

	@Test
	public void testPostTwoCredits() {
		// given
		String customer1 = "CUSTOMER_1";
		BigDecimal amount = BigDecimal.TEN;
		// when
		CustomerBalance balance = postCredits(2, 0, 0, customer1, amount);
		// then
		assert balance != null;
		Money creditAmount = balance.getBalanceAmounts().get(GIFTCARD).get(0);
		assert creditAmount.getAmount().compareTo(amount) == 0;
		assert creditAmount.getCurrency().equals(USD.toString());
		creditAmount = balance.getBalanceAmounts().get(GIFTCARD).get(1);
		assert creditAmount.getAmount().compareTo(amount) == 0;
		assert creditAmount.getCurrency().equals(USD.toString());
		assert bankController.debitHistory(customer1).getDebits().isEmpty() == true;
	}

	@Test
	public void testPostCreditsAndDebit() {
		// given
		String customer1 = "CUSTOMER_1";
		BigDecimal creditAmountValue = BigDecimal.TEN;
		// when
		CustomerBalance balance = postCredits(2, 2, 2, customer1, creditAmountValue);
		// then
		assert balance != null;
		Money creditAmount = balance.getBalanceAmounts().get(GIFTCARD).get(0);
		assert creditAmount.getAmount().compareTo(creditAmountValue) == 0;
		assert creditAmount.getCurrency().equals(USD.toString());
		creditAmount = balance.getBalanceAmounts().get(GIFTCARD).get(1);
		assert creditAmount.getAmount().compareTo(creditAmountValue) == 0;
		assert creditAmount.getCurrency().equals(USD.toString());
		assert bankController.debitHistory(customer1).getDebits().isEmpty() == true;

		// given
		String invoice1 = "INV_1";
		BigDecimal debitAmountValue = BigDecimal.valueOf(35);
		DebitAmount debitAmount = new DebitAmount(invoice1, new Money(debitAmountValue, USD.toString()));

		// when
		try {
			balance = bankController.debit(customer1, debitAmount);
		} catch (Exception e) {
			fail("Error performing debit operation.");
		}

		// then
		assert bankController.debitHistory(customer1).getDebits() != null;
		assert balance.getBalanceAmounts().get(GIFTCARD).isEmpty() == true;
		assert balance.getBalanceAmounts().get(PROMOTION).size() == 1;
		assert balance.getBalanceAmounts().get(CASH).size() == 2;
	}

	@Test
	public void testDedupePostTwoCredits() {
		// given
		String customer1 = "CUSTOMER_1";
		BigDecimal amount = BigDecimal.TEN;
		// when
		CustomerBalance balance = postCredits(2, 0, 0, customer1, amount);
		// dedupe
		balance = postCredits(2, 0, 0, customer1, amount);
		// then
		assert balance != null;
		Money creditAmount = balance.getBalanceAmounts().get(GIFTCARD).get(0);
		assert creditAmount.getAmount().compareTo(amount) == 0;
		assert creditAmount.getCurrency().equals(USD.toString());
		creditAmount = balance.getBalanceAmounts().get(GIFTCARD).get(1);
		assert creditAmount.getAmount().compareTo(amount) == 0;
		assert creditAmount.getCurrency().equals(USD.toString());
		assert bankController.debitHistory(customer1).getDebits().isEmpty() == true;
	}

	@Test
	public void testDedupePostCreditsAndDebit() {
		// given
		String customer1 = "CUSTOMER_1";
		BigDecimal creditAmountValue = BigDecimal.TEN;
		// when
		CustomerBalance balance = postCredits(2, 2, 2, customer1, creditAmountValue);
		// dedupe credit
		balance = postCredits(2, 2, 2, customer1, creditAmountValue);
		// then
		assert balance != null;
		Money creditAmount = balance.getBalanceAmounts().get(GIFTCARD).get(0);
		assert creditAmount.getAmount() == creditAmountValue;
		assert creditAmount.getCurrency().equals(USD.toString());
		creditAmount = balance.getBalanceAmounts().get(GIFTCARD).get(1);
		assert creditAmount.getAmount() == creditAmountValue;
		assert creditAmount.getCurrency().equals(USD.toString());
		assert bankController.debitHistory(customer1).getDebits().isEmpty() == true;

		// given
		String invoice1 = "INV_1";
		BigDecimal debitAmountValue = BigDecimal.valueOf(35);
		DebitAmount debitAmount = new DebitAmount(invoice1, new Money(debitAmountValue, USD.toString()));

		// when
		try {
			balance = bankController.debit(customer1, debitAmount);
			// dedupe debit
			balance = bankController.debit(customer1, debitAmount);
		} catch (Exception e) {
			fail("Error performing debit operation.");
		}

		// then
		assert bankController.debitHistory(customer1).getDebits() != null;
		assert balance.getBalanceAmounts().get(GIFTCARD).isEmpty() == true;
		assert balance.getBalanceAmounts().get(PROMOTION).size() == 1;
		assert balance.getBalanceAmounts().get(CASH).size() == 2;

		// dedupe debit request
		// when
		try {
			balance = bankController.debit(customer1, debitAmount);
			// dedupe debit
			balance = bankController.debit(customer1, debitAmount);
		} catch (Exception e) {
			fail("Error performing debit operation.");
		}

		// then
		assert bankController.debitHistory(customer1).getDebits() != null;
		assert balance.getBalanceAmounts().get(GIFTCARD).isEmpty() == true;
		assert balance.getBalanceAmounts().get(PROMOTION).size() == 1;
		assert balance.getBalanceAmounts().get(CASH).size() == 2;
	}

	// Utility method to post credits
	private CustomerBalance postCredits(int numGiftcard, int numPromo, int numCash, String customerId, BigDecimal amount) {
		CustomerBalance balance = null;
		for (int i = 0; i < numGiftcard; i++) {
			balance = bankController.postCredit(customerId,
					new CreditAmount(GIFTCARD, new Money(amount, USD.toString()), "TX_"+i));
			assert balance != null;
			assert balance.getBalanceAmounts().get(GIFTCARD).get(i).getAmount().equals(amount);
		}
		for (int i = 0; i < numPromo; i++) {
			balance = bankController.postCredit(customerId,
					new CreditAmount(PROMOTION, new Money(amount, USD.toString()), "TX_"+i));
			assert balance != null;
			assert balance.getBalanceAmounts().get(PROMOTION).get(i).getAmount().equals(amount);
		}
		for (int i = 0; i < numCash; i++) {
			balance = bankController.postCredit(customerId,
					new CreditAmount(CASH, new Money(amount, USD.toString()), "TX_"+i));
			assert balance != null;
			assert balance.getBalanceAmounts().get(CASH).get(i).getAmount().equals(amount);
		}

		// after
		Map<CreditType, List<Money>> map = balance.getBalanceAmounts();
		if (map.containsKey(GIFTCARD)) assert balance.getBalanceAmounts().get(GIFTCARD).size() == numGiftcard;
		if (map.containsKey(PROMOTION)) assert balance.getBalanceAmounts().get(PROMOTION).size() == numPromo;
		if (map.containsKey(CASH)) assert balance.getBalanceAmounts().get(CASH).size() == numCash;

		return balance;
	}
}

