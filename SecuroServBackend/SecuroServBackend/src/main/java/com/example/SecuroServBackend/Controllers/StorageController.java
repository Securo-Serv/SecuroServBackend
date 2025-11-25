package com.example.SecuroServBackend.Controllers;

import com.example.SecuroServBackend.Configuration.JwtUtil;
import com.example.SecuroServBackend.Entity.AuthUser;
import com.example.SecuroServBackend.Entity.User;
import com.example.SecuroServBackend.Repository.AuditLogRepo;
import com.example.SecuroServBackend.Repository.AuthUserRepo;
import com.example.SecuroServBackend.Repository.FileEntityRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
public class StorageController {

    private final FileEntityRepo fileEntityRepo;
    private final JwtUtil jwtUtil;
    private final AuthUserRepo authRepository;

    @GetMapping("/used")
    public Map<String, Object> getUserStorageUsed(@RequestHeader("Authorization") String token) {
        String jwtToken = token.replace("Bearer ", "");
        String email = jwtUtil.extractUsername(jwtToken);
        System.out.println("called called ");
        AuthUser authUser = authRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        User user = authUser.getUser();
        UUID userId = user.getUserId();


        long totalBytes = fileEntityRepo.getTotalStorageUsedByUser(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("usedBytes", totalBytes);
        response.put("usedMB", totalBytes / (1024.0 * 1024.0));
        response.put("usedGB", totalBytes / (1024.0 * 1024.0 * 1024.0));
        System.out.println("done done");
        return response;
    }
}
