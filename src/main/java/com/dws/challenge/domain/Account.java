package com.dws.challenge.domain;

import com.dws.challenge.exception.InsufficientBalanceException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;


@Data
public class Account {

  // TODO: For this operations better to separate DTO and DAO's
  @JsonIgnore
  private final ReadWriteLock lock = new ReentrantReadWriteLock();

  @NotNull
  @NotEmpty
  private final String accountId;

  @NotNull
  @Min(value = 0, message = "Initial balance must be positive.")
  private BigDecimal balance;

  public Account(String accountId) {
    this.accountId = accountId;
    this.balance = BigDecimal.ZERO;
  }

  @JsonCreator
  public Account(@JsonProperty("accountId") String accountId,
    @JsonProperty("balance") BigDecimal balance) {
    this.accountId = accountId;
    this.balance = balance;
  }

  public void deposit(BigDecimal amount) {
    lock.writeLock().lock();
    try {
      if (balance.compareTo(amount) >= 0) {
        balance = balance.add(amount);
      } else {
        throw new InsufficientBalanceException("Insufficient balance for deposit!");
      }
    } finally {
      lock.writeLock().unlock();
    }
  }

  public void withdraw(BigDecimal amount) {
    lock.writeLock().lock();
    try {
      if (balance.compareTo(amount) >= 0) {
        balance = balance.subtract(amount);
      } else {
        throw new InsufficientBalanceException("Insufficient balance for withdrawal!");
      }
    } finally {
      lock.writeLock().unlock();
    }
  }

  public void transfer(Account toAccount, BigDecimal amount) {
    Account from = this;
    Account to = toAccount;

    // keep right order of account to avoid deadlock
    if (accountId.compareTo(toAccount.getAccountId()) > 0) {
      from = toAccount;
      to = this;
    }

    try {
      from.lock.writeLock().lock();
      to.lock.writeLock().lock();

      if (balance.compareTo(amount) >= 0) {
        withdraw(amount);
        toAccount.deposit(amount);
      } else {
        throw new InsufficientBalanceException("Insufficient balance for transfer!");
      }
    } finally {
      to.lock.writeLock().unlock();
      from.lock.writeLock().unlock();
    }
  }
}
