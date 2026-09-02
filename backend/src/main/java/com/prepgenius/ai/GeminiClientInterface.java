package com.prepgenius.ai;

public interface GeminiClientInterface {
    String generateContent(String prompt);

    boolean isConfigured();

    String getModel();
}
