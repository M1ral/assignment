package com.netflix.billing.bank;

import com.netflix.billing.bank.controller.BankController;
import com.netflix.billing.bank.controller.wire.account.AccountExecutorService;
import com.netflix.billing.bank.controller.wire.account.AccountManager;
import com.netflix.billing.bank.controller.wire.account.CustomerBalance;
import com.netflix.billing.bank.controller.wire.account.Money;
import com.netflix.billing.bank.controller.wire.credit.CreditAmount;
import com.netflix.billing.bank.controller.wire.credit.CreditType;
import com.netflix.billing.bank.controller.wire.debit.DebitAmount;
import com.netflix.billing.bank.controller.wire.debit.DebitHistory;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

	@Autowired
	public AccountExecutorService accountExecutorService;

	@After
	public void afterTest() {
		accountManager.clear();
	}

	private ExecutorService executorService = Executors.newFixedThreadPool(100);

	@Test(expected = Error.class)
	public void testNullInputsPostCredit() {
		// given
		String customer1 = "CUSTOMER_1";
		BigDecimal amount = BigDecimal.TEN;
		// when
		CustomerBalance balance = bankController.postCredit(customer1, null);
		fail("Test should have thrown invalid input error");
	}

	@Test(expected = Error.class)
	public void testInvalidInputsPostCredit() {
		// given
		String customer1 = "CUSTOMER_1";
		BigDecimal amount = BigDecimal.TEN;
		// when
		CustomerBalance balance = bankController
				.postCredit(customer1, new CreditAmount(null, null, null));
		fail("Test should have thrown invalid input error");
	}

	@Test(expected = Error.class)
	public void testNullInputsPostDebit() {
		// given
		String customer1 = "CUSTOMER_1";
		// when
		CustomerBalance balance = bankController.debit(customer1, null);
		fail("Test should have thrown invalid input error");
	}

	@Test(expected = Error.class)
	public void testInvalidInputsPostDebit() {
		// given
		String customer1 = "CUSTOMER_1";
		// when
		CustomerBalance balance = bankController.debit(customer1, new DebitAmount("", null));
		fail("Test should have thrown invalid input error");
	}

	@Test(expected = Error.class)
	public void testNullInputsGetBalance() {
		// when
		CustomerBalance balance = bankController.getBalance(null);
		fail("Test should have thrown invalid input error");
	}

	@Test(expected = Error.class)
	public void testInvalidInputsGetBalance() {
		// when
		CustomerBalance balance = bankController.getBalance("");
		fail("Test should have thrown invalid input error");
	}

	@Test(expected = Error.class)
	public void testNullInputsDebitHistory() {
		// when
		DebitHistory history = bankController.debitHistory(null);
		fail("Test should have thrown invalid input error");
	}

	@Test(expected = Error.class)
	public void testInvalidInputsDebitHistory() {
		// when
		DebitHistory history = bankController.debitHistory("");
		fail("Test should have thrown invalid input error");
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

		// when - use same transactionIds for customer 2
		String customer2 = "CUSTOMER_2";
		balance = postCredits(2, 2, 2, customer2, creditAmountValue);
		// then
		assert balance != null;
		assert balance.getBalanceAmounts().get(GIFTCARD).size() == 2;
		assert balance.getBalanceAmounts().get(PROMOTION).size() == 2;
		assert balance.getBalanceAmounts().get(CASH).size() == 2;

		creditAmount = balance.getBalanceAmounts().get(GIFTCARD).get(0);
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

		// when - use same invoiceId for customer2
		try {
			balance = bankController.debit(customer2, debitAmount);
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
	public void testDebitHistory() {
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
		assert creditAmount.getAmount().equals(creditAmountValue);
		assert creditAmount.getCurrency().equals(USD.toString());
		creditAmount = balance.getBalanceAmounts().get(GIFTCARD).get(1);
		assert creditAmount.getAmount().equals(creditAmountValue);
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
		assert bankController.debitHistory(customer1).getDebits().size() == 4;
		assert balance.getBalanceAmounts().get(GIFTCARD).isEmpty() == true;
		assert balance.getBalanceAmounts().get(PROMOTION).size() == 1;
		assert balance.getBalanceAmounts().get(CASH).size() == 2;
	}

	@Test(expected = Error.class)
	public void testInsufficientFundsPostCreditsAndDebit() {
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
		BigDecimal debitAmountValue = BigDecimal.valueOf(100);
		DebitAmount debitAmount = new DebitAmount(invoice1, new Money(debitAmountValue, USD.toString()));

		// when
		try {
			balance = bankController.debit(customer1, debitAmount);
		} catch (Exception e) {
			fail("Error performing debit operation.");
		}
		fail("Test should have thrown Insufficient funds error.");
	}

	@Test
	public void testPostCreditDebitMultiThread() {
		// given
		String customer1 = "CUSTOMER_1", invoice1 = "INV_1", invoice2 = "INV_2";
		String customer2 = "CUSTOMER_2", invoice3 = "INV_3", invoice4 = "INV_4";
		BigDecimal creditAmountValue = BigDecimal.TEN;

		// when
		Callable<CustomerBalance> postCreditCustomer1 = () -> {
			return postCredits(3, 4, 5, customer1, creditAmountValue); // 120$ credit
		};
		Callable<CustomerBalance> postDebitCustomer1_1 = () -> {
			return bankController.debit(customer1,
					new DebitAmount(invoice1, new Money(BigDecimal.valueOf(35), USD.toString())));
		};
		Callable<CustomerBalance> postDebitCustomer1_2 = () -> {
			return bankController.debit(customer1,
					new DebitAmount(invoice2, new Money(BigDecimal.valueOf(25), USD.toString()))); // total 60$ debtit
		};
		Callable<CustomerBalance> postCreditCustomer2 = () -> {
			return postCredits(3, 4, 5, customer2, creditAmountValue); // 120$ credit
		};
		Callable<CustomerBalance> postDebitCustomer2_1 = () -> {
			return bankController.debit(customer2,
					new DebitAmount(invoice3, new Money(BigDecimal.valueOf(35), USD.toString())));
		};
		Callable<CustomerBalance> postDebitCustomer2_2 = () -> {
			return bankController.debit(customer2,
					new DebitAmount(invoice4, new Money(BigDecimal.valueOf(45), USD.toString()))); // total 80 $ debit
		};

		// execute postCredit and postDebit in multiThreaded mode
		accountExecutorService.execute(postCreditCustomer1);
		accountExecutorService.execute(postCreditCustomer2);
		accountExecutorService.execute(postDebitCustomer2_2);
		accountExecutorService.execute(postDebitCustomer1_2);
		CustomerBalance balance_customer1 = accountExecutorService.execute(postDebitCustomer1_1);
		CustomerBalance balance_customer2 = accountExecutorService.execute(postDebitCustomer2_1);

		// then customer1
		assert balance_customer1 != null;
		assert balance_customer1.getBalanceAmounts().get(GIFTCARD).isEmpty() == true;
		assert balance_customer1.getBalanceAmounts().get(PROMOTION).size() == 1;
		assert balance_customer1.getBalanceAmounts().get(CASH).size() == 5;
		assert bankController.debitHistory(customer1).getDebits().size() == 7;

		// then customer2
		assert balance_customer2 != null;
		assert balance_customer2.getBalanceAmounts().get(GIFTCARD).isEmpty() == true;
		assert balance_customer2.getBalanceAmounts().get(PROMOTION).isEmpty() == true;
		assert balance_customer1.getBalanceAmounts().get(CASH).size() == 5;
		assert bankController.debitHistory(customer2).getDebits().size() == 9;
	}

	// Utility method to post credits
	private CustomerBalance postCredits(int numGiftcard, int numPromo, int numCash, String customerId, BigDecimal amount) {
		CustomerBalance balance = null;
		// GIFTCARD
		for (int i = 0; i < numGiftcard; i++) {
			balance = bankController.postCredit(customerId,
					new CreditAmount(GIFTCARD, new Money(amount, USD.toString()), "TX_"+i));
			assert balance != null;
			assert balance.getBalanceAmounts().get(GIFTCARD).get(i).getAmount().equals(amount);
		}
		// PROMOTION
		for (int i = 0; i < numPromo; i++) {
			balance = bankController.postCredit(customerId,
					new CreditAmount(PROMOTION, new Money(amount, USD.toString()), "TX_"+i));
			assert balance != null;
			assert balance.getBalanceAmounts().get(PROMOTION).get(i).getAmount().equals(amount);
		}
		// CASH
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