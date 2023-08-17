package com.dws.challenge;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.AccountDoesNotExistException;
import com.dws.challenge.exception.InsufficientBalanceException;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.NotificationService;
import com.dws.challenge.service.TransferService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;


@ExtendWith(SpringExtension.class)
@SpringBootTest
public class TransferServiceTest {

    @Autowired
    private AccountsService accountsService;

    @Autowired
    private TransferService transferService;

    @MockBean
    private NotificationService notificationService;

    @AfterEach
    void afterEach() {
        accountsService.clearAccounts();
    }

    @Test
    void fromAccountDoesNotExist() {
        String fromId = "Id-123";
        String toId = "Id-456";

        accountsService.createAccount(new Account(toId, new BigDecimal("100000")));

        try {
            transferService.transfer(fromId, toId, new BigDecimal("1000"));
            fail("Should have failed when getting a non-existent account");
        } catch (AccountDoesNotExistException ex) {
            assertThat(ex.getMessage()).isEqualTo(String.format("Account '%s' doesn't exist!", fromId));
        }
    }

    @Test
    void toAccountDoesNotExist() {
        String fromId = "Id-123";
        String toId = "Id-456";

        accountsService.createAccount(new Account(fromId, new BigDecimal("100000")));

        try {
            transferService.transfer(fromId, toId, new BigDecimal("1000"));
            fail("Should have failed when getting a non-existent account");
        } catch (AccountDoesNotExistException ex) {
            assertThat(ex.getMessage()).isEqualTo(String.format("Account '%s' doesn't exist!", toId));
        }
    }

    @Test
    void insufficientBalanceForTransfer() {
        String fromId = "Id-123";
        String toId = "Id-456";

        accountsService.createAccount(new Account(fromId, new BigDecimal("100")));
        accountsService.createAccount(new Account(toId, new BigDecimal("100000")));

        try {
            transferService.transfer(fromId, toId, new BigDecimal("1000"));
            fail("Should have failed when trying to transfer more funds than have");
        } catch (InsufficientBalanceException ex) {
            assertThat(ex.getMessage()).isEqualTo("Insufficient balance for transfer!");
        }
    }

    @Test
    public void testMoneyTransferConcurrency() throws InterruptedException {
        Mockito.doNothing().when(notificationService).notifyAboutTransfer(Mockito.any(), Mockito.any());

        String fromId = "Id-123";
        String toId = "Id-456";

        accountsService.createAccount(new Account(fromId, new BigDecimal("1000")));
        accountsService.createAccount(new Account(toId, new BigDecimal("1000")));

        int numThreads = 10;

        CountDownLatch latch = new CountDownLatch(numThreads);
        List<Thread> threads = new ArrayList<>();

        // transfer from 'from' to 'to'
        for (int i = 0; i < numThreads; i++) {
            Thread threadFromTo = new Thread(() -> {
                for (int j = 0; j < 10; j++) { // Perform multiple transfers per thread
                    transferService.transfer(fromId, toId, new BigDecimal("10"));
                }
                latch.countDown();
            });
            threads.add(threadFromTo);
        }


        for (Thread thread : threads) {
            thread.start();
        }

        latch.await(); // Wait for all threads to finish

        assertThat(accountsService.getAccount(fromId).getBalance()).isEqualTo(new BigDecimal("0"));
        assertThat(accountsService.getAccount(toId).getBalance()).isEqualTo(new BigDecimal("2000"));

        Mockito.verify(notificationService, Mockito.times(200)).notifyAboutTransfer(Mockito.any(), Mockito.any());
    }
}
