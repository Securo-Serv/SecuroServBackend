package com.example.SecuroServBackend.Controllers;

import com.example.SecuroServBackend.Configuration.JwtUtil;
import com.example.SecuroServBackend.Entity.AuthUser;
import com.example.SecuroServBackend.Entity.User;
import com.example.SecuroServBackend.Repository.AuthUserRepo;
import com.example.SecuroServBackend.Service.UserServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserServices userServices;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthUserRepo authUserRepo;

    @GetMapping("/allData")
    public ResponseEntity<?> showAllFiles(@RequestHeader("Authorization") String token) {
        System.out.println("🎶 Fetching all user vault data...");

        String jwtToken = token.replace("Bearer ", "");
        String email = jwtUtil.extractUsername(jwtToken);

        AuthUser authUser = authUserRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        User user = authUser.getUser();

        return ResponseEntity.ok(userServices.getUserVault(user));
    }
}
