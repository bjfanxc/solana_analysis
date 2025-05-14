package com.xps.solana.service;

import com.xps.solana.model.TransactionInfo;
import com.xps.solana.util.TransactionParser;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SolanaService {

    private final OkHttpClient client;
    private final Gson gson;
    private final ObjectMapper objectMapper;
    private static final String SOLANA_RPC_URL = "https://solana-rpc.publicnode.com";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public SolanaService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, Object> getLatestBlock() throws IOException {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("jsonrpc", "2.0");
        requestBody.addProperty("id", 1);
        requestBody.addProperty("method", "getLatestBlockhash");
        
        com.google.gson.JsonArray paramsArray = new com.google.gson.JsonArray();
        requestBody.add("params", paramsArray);

        Request request = new Request.Builder()
                .url(SOLANA_RPC_URL)
                .post(RequestBody.create(requestBody.toString(), JSON))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected response " + response);
            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            return objectMapper.convertValue(jsonNode, Map.class);
        }
    }

    public JsonObject getTransaction(String signature) throws IOException {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("jsonrpc", "2.0");
        requestBody.addProperty("id", 1);
        requestBody.addProperty("method", "getTransaction");
        
        com.google.gson.JsonArray paramsArray = new com.google.gson.JsonArray();
        paramsArray.add(signature);
        JsonObject config = new JsonObject();
        config.addProperty("encoding", "json");
        config.addProperty("maxSupportedTransactionVersion", 0);
        paramsArray.add(config);
        requestBody.add("params", paramsArray);

        Request request = new Request.Builder()
                .url(SOLANA_RPC_URL)
                .post(RequestBody.create(requestBody.toString(), JSON))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected response " + response);
            String responseBody = response.body().string();
            log.info("responseBody: {}", responseBody);
            return gson.fromJson(responseBody, JsonObject.class);
        }
    }

    public List<TransactionInfo> getBlockTransactions(String slot) throws IOException {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("jsonrpc", "2.0");
        requestBody.addProperty("id", 1);
        requestBody.addProperty("method", "getBlock");
        
        com.google.gson.JsonArray paramsArray = new com.google.gson.JsonArray();
        paramsArray.add(Long.parseLong(slot));
        
        JsonObject config = new JsonObject();
        config.addProperty("encoding", "json");
        config.addProperty("transactionDetails", "full");
        config.addProperty("maxSupportedTransactionVersion", 0);
        paramsArray.add(config);
        
        requestBody.add("params", paramsArray);

        Request request = new Request.Builder()
                .url(SOLANA_RPC_URL)
                .post(RequestBody.create(requestBody.toString(), JSON))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected response " + response);
            String responseBody = response.body().string();
            log.info("responseBody: {}", responseBody);
            JsonObject blockData = gson.fromJson(responseBody, JsonObject.class);
            return TransactionParser.parseBlockTransactions(blockData);
        }
    }

    public TransactionInfo getTransactionInfo(String signature) throws IOException {
        JsonObject transactionData = getTransaction(signature);
        if (transactionData.has("result") && !transactionData.get("result").isJsonNull()) {
            return TransactionParser.parseTransaction(transactionData.getAsJsonObject("result"));
        }
        return null;
    }
}