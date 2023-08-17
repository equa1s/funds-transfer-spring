package com.dws.challenge.web;

import com.dws.challenge.dto.TransferDto;
import com.dws.challenge.exception.AccountDoesNotExistException;
import com.dws.challenge.exception.InsufficientBalanceException;
import com.dws.challenge.service.TransferService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1/transfer")
@Slf4j
public class TransferController {
    private final TransferService transferService;

    @Autowired
    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> transfer(@RequestBody @Valid TransferDto transferDto) {
        log.info("Transfer dto: {}", transferDto);
        transferService.transfer(transferDto.getFrom(), transferDto.getTo(), transferDto.getAmount());
        return ResponseEntity.accepted().build();
    }

    @ExceptionHandler(value = { AccountDoesNotExistException.class, InsufficientBalanceException.class })
    public ResponseEntity<Object> exceptionHandler(RuntimeException e) {
        log.error(e.getMessage(), e);
        // keep it simple as a simple string, just for a test scenario
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
