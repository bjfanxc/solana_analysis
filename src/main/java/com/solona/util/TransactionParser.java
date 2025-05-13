package com.solona.util;

import com.solona.model.TransactionInfo;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class TransactionParser {

    public static List<TransactionInfo> parseBlockTransactions(JsonObject blockData) {
        List<TransactionInfo> transactions = new ArrayList<>();
        
        if (blockData.has("result") && !blockData.get("result").isJsonNull()) {
            JsonObject result = blockData.getAsJsonObject("result");
            if (result.has("transactions") && !result.get("transactions").isJsonNull()) {
                JsonArray txArray = result.getAsJsonArray("transactions");
                
                for (JsonElement txElement : txArray) {
                    JsonObject transaction = txElement.getAsJsonObject();
                    TransactionInfo txInfo = parseTransaction(transaction);
                    if (txInfo != null) {
                        transactions.add(txInfo);
                    }
                }
            }
        }
        
        return transactions;
    }

    public static TransactionInfo parseTransaction(JsonObject transaction) {
        try {
            TransactionInfo txInfo = new TransactionInfo();
            
            // 获取交易基本信息
            if (transaction.has("transaction")) {
                JsonObject txData = transaction.getAsJsonObject("transaction");
                if (txData.has("signatures") && txData.getAsJsonArray("signatures").size() > 0) {
                    txInfo.setSignature(txData.getAsJsonArray("signatures").get(0).getAsString());
                }
            }
            
            // 获取交易状态
            if (transaction.has("meta")) {
                JsonObject meta = transaction.getAsJsonObject("meta");
                txInfo.setStatus(meta.get("err") == null ? "success" : "failed");
                
                // 解析交易前后账户余额变化来确定转账方向
                if (meta.has("preBalances") && meta.has("postBalances")) {
                    JsonArray preBalances = meta.getAsJsonArray("preBalances");
                    JsonArray postBalances = meta.getAsJsonArray("postBalances");
                    
                    // 获取账户地址
                    if (transaction.has("transaction")) {
                        JsonObject txData = transaction.getAsJsonObject("transaction");
                        if (txData.has("message")) {
                            JsonObject message = txData.getAsJsonObject("message");
                            if (message.has("accountKeys")) {
                                JsonArray accountKeys = message.getAsJsonArray("accountKeys");
                                
                                // 通过比较余额变化来确定发送方和接收方
                                for (int i = 0; i < preBalances.size() && i < accountKeys.size(); i++) {
                                    long preBal = preBalances.get(i).getAsLong();
                                    long postBal = postBalances.get(i).getAsLong();
                                    String address = accountKeys.get(i).getAsString();
                                    
                                    if (preBal > postBal) {
                                        txInfo.setFromAddress(address);
                                        txInfo.setAmount(String.valueOf((preBal - postBal) / 1000000000.0)); // Convert lamports to SOL
                                    } else if (postBal > preBal) {
                                        txInfo.setToAddress(address);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // 获取区块时间和槽位
            if (transaction.has("blockTime")) {
                txInfo.setBlockTime(transaction.get("blockTime").getAsString());
            }
            if (transaction.has("slot")) {
                txInfo.setSlot(transaction.get("slot").getAsString());
            }
            
            return txInfo;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}