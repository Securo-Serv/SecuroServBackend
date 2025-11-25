package com.example.SecuroServBackend.Service;

import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VaultSessionCache {
    private final Map<String, SecretKeySpec> activeVaultKeys = new ConcurrentHashMap<>();

    public void storeRootKey(String token, SecretKeySpec rootKey) {
        activeVaultKeys.put(token, rootKey);
    }

    public SecretKeySpec getRootKey(String token) {
        return activeVaultKeys.get(token);
    }

    public void removeRootKey(String token) {
        activeVaultKeys.remove(token);
    }

    public void clearAll() {
        activeVaultKeys.clear();
    }
}
