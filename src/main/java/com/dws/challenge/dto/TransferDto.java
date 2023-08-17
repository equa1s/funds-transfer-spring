package com.dws.challenge.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.math.BigDecimal;

@Data
public class TransferDto {

    @NotBlank
    private String from;

    @NotBlank
    private String to;

    @NotNull
    @Min(value = 0, message = "Initial balance must be positive.")
    private BigDecimal amount;

}
