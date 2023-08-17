package com.dws.challenge.service;

import java.math.BigDecimal;

public interface TransferService {
    void transfer(String accountFromId, String accountToId, BigDecimal amount);
}
