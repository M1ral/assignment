package com.netflix.billing.bank;

import com.netflix.billing.bank.controller.BankController;
import com.netflix.billing.bank.controller.wire.CustomerBalance;
import com.netflix.billing.bank.controller.wire.Money;
import com.netflix.billing.bank.controller.wire.credit.CreditAmount;
import com.netflix.billing.bank.controller.wire.credit.CreditType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static com.netflix.billing.bank.controller.wire.Currency.USD;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BillingbankApplicationTests {

	@Autowired
	public BankController bankController;

	@Test
	public void contextLoads() {
		assert bankController.getBalance(String.valueOf(Integer.MIN_VALUE)) == null;
	}

	@Test
	public void testPostCredit() {
		// given
		String customerId = "CUST_1";
		CreditAmount amount = new CreditAmount(CreditType.GIFTCARD, new Money(10l, USD.toString()));
		// when
		CustomerBalance balance = bankController.postCredit(customerId, amount);
		// then
		assert balance != null;
		Money creditAmount = balance.getBalanceAmounts().get(CreditType.GIFTCARD).get(0);
		assert creditAmount.getAmount() == 10l;
		assert creditAmount.getCurrency().equals(USD.toString());
	}
}

