package com.solona.controller;

import com.solona.model.TransactionInfo;
import com.solona.service.SolanaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/solana")
@RequiredArgsConstructor
public class SolanaController {

    private final SolanaService solanaService;

    @GetMapping("/latest-block")
    public Object getLatestBlock() throws IOException {
        return solanaService.getLatestBlock();
    }

    @GetMapping("/block/{slot}/transactions")
    public List<TransactionInfo> getBlockTransactions(@PathVariable String slot) throws IOException {
        return solanaService.getBlockTransactions(slot);
    }

    @GetMapping("/transaction/{signature}")
    public TransactionInfo getTransaction(@PathVariable String signature) throws IOException {
        return solanaService.getTransactionInfo(signature);
    }
}