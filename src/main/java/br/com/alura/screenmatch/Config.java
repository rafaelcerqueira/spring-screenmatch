package br.com.alura.screenmatch;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Config {
    public static String getApiKey() {
        String apiKey = null;
        try (BufferedReader br = new BufferedReader(new FileReader("api_key.txt"))) {
            apiKey = br.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return apiKey;
    }
}
