package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.AccountDoesNotExistException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


@Service
public class TransferServiceImpl implements TransferService {

    private final AccountsService accountsService;
    private final NotificationService notificationService;

    @Autowired
    public TransferServiceImpl(AccountsService accountsService, NotificationService notificationService) {
        this.accountsService = accountsService;
        this.notificationService = notificationService;
    }

    @Override
    public void transfer(String fromId, String toId, BigDecimal amount) {
        Account from = accountsService.getAccount(fromId);
        if (from == null) {
            throw new AccountDoesNotExistException(String.format("Account '%s' doesn't exist!", fromId));
        }

        Account to = accountsService.getAccount(toId);
        if (to == null) {
            throw new AccountDoesNotExistException(String.format("Account '%s' doesn't exist!", toId));
        }

        from.transfer(to, amount);

        // everything is good, send a notification
        notify(from, to, amount);
    }

    private void notify(Account from, Account to, BigDecimal amount) {
        notificationService.notifyAboutTransfer(from,
                String.format("The transfer to '%s' was successful. Amount transferred: %s", to.getAccountId(), amount.doubleValue()));
        notificationService.notifyAboutTransfer(to,
                String.format("The transfer from '%s' was successful. Amount transferred: %s", from.getAccountId(), amount.doubleValue()));
    }

}
