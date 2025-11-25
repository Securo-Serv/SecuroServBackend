package com.example.SecuroServBackend.Controllers;

import com.example.SecuroServBackend.Configuration.JwtUtil;
import com.example.SecuroServBackend.Entity.AuthUser;
import com.example.SecuroServBackend.Entity.User;
import com.example.SecuroServBackend.Repository.AuthUserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserController {

    private final JwtUtil jwtUtil;
    private final AuthUserRepo authUserRepo;


    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String token) {

        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("❌ Missing or invalid token");
            }


            String jwt = token.substring(7);
            String email = jwtUtil.extractUsername(jwt);


            Optional<AuthUser> authUserOpt = authUserRepo.findByEmail(email);
            if (authUserOpt.isEmpty()) {
                return ResponseEntity.status(404).body("❌ User not found for email: " + email);
            }

            AuthUser authUser = authUserOpt.get();
            User user = authUser.getUser();


            String role = "ROLE_USER";
            if (user != null && user.getRole() != null) {
                role = user.getRole().name();
            } else if (authUser.getRole() != null) {
                role = authUser.getRole();
            }


            UserProfileResponse response = new UserProfileResponse(
                    authUser.getEmail(),
                    (user != null) ? user.getUserId() : null,
                    role,
                    (user != null) ? user.getFolders().size() : 0,
                    (user != null) ? user.getRootFiles().size() : 0
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(401).body("❌ Invalid token or error: " + e.getMessage());
        }
    }


    record UserProfileResponse(
            String email,
            java.util.UUID userId,
            String role,
            int totalFolders,
            int totalFiles
    ) {}
}
