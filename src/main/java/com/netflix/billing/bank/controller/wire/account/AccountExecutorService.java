package com.netflix.billing.bank.controller.wire.account;

import org.springframework.stereotype.Component;

import java.util.concurrent.*;

@Component
public class AccountExecutorService {

    // number of threads executorService runs, move this config param to ApplicationConfig
    private static final int NUM_THREADS = 100;

    private ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);

    /**
     * Execute callableTask using ExecutorService
     * @param callableTask
     * @return CustomerBalance
     */
    public CustomerBalance execute(Callable<CustomerBalance> callableTask) {
        if (null == callableTask) {
            throw new Error("Invalid callable function to execute");
        }

        Future<CustomerBalance> result = executorService.submit(callableTask);
        CustomerBalance balance;
        try {
            balance = result.get();
        } catch (InterruptedException e) {
            throw new Error("Credit Interrupted.");
        } catch (ExecutionException e) {
            throw new Error("Credit Interrupted.");
        }
        return balance;
    }
}
