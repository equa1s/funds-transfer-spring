package com.dws.challenge;

import com.dws.challenge.domain.Account;
import com.dws.challenge.service.AccountsService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;


@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
public class TransferControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private AccountsService accountsService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void prepareMockMvc() {
        this.mockMvc = webAppContextSetup(this.webApplicationContext).build();
        accountsService.clearAccounts();
    }

    @Test
    void transferWhenFromAccountDoesNotExist() throws Exception {
        accountsService.createAccount(new Account("Id-456"));

        this.mockMvc.perform(post("/v1/transfer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"from\":\"Id-123\",\"to\":\"Id-456\", \"amount\": 1000}"))
                    .andExpect(status().isBadRequest());
    }

    @Test
    void transferWhenToAccountDoesNotExist() throws Exception {
        accountsService.createAccount(new Account("Id-123"));

        this.mockMvc.perform(post("/v1/transfer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"from\":\"Id-123\",\"to\":\"Id-456\", \"amount\": 1000}"))
                    .andExpect(status().isBadRequest());
    }

    @Test
    void transferWhenNotEnoughMoney() throws Exception {
        accountsService.createAccount(new Account("Id-123", new BigDecimal("100")));
        accountsService.createAccount(new Account("Id-456"));

        this.mockMvc.perform(post("/v1/transfer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"from\":\"Id-123\",\"to\":\"Id-456\", \"amount\": 1000}"))
                    .andExpect(status().isBadRequest());
    }

    @Test
    void transferWhenEnoughMoney() throws Exception {
        accountsService.createAccount(new Account("Id-123", new BigDecimal("100")));
        accountsService.createAccount(new Account("Id-456", new BigDecimal("10")));

        this.mockMvc.perform(post("/v1/transfer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"from\":\"Id-123\",\"to\":\"Id-456\", \"amount\": 10}"))
                    .andExpect(status().isAccepted());
    }
}
