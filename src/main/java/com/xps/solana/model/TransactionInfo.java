package com.xps.solana.model;

import lombok.Data;

@Data
public class TransactionInfo {
    private String signature;
    private String fromAddress;
    private String toAddress;
    private String amount;
    private String blockTime;
    private String slot;
    private String status;
}