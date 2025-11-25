package com.example.SecuroServBackend.Controllers;

import com.example.SecuroServBackend.Configuration.JwtUtil;
import com.example.SecuroServBackend.Configuration.OtpGenerator;
import com.example.SecuroServBackend.Configuration.UserPrinciple;
import com.example.SecuroServBackend.DTOs.AuthUserDTO;
import com.example.SecuroServBackend.DTOs.OTP;
import com.example.SecuroServBackend.Entity.AuthUser;
import com.example.SecuroServBackend.Entity.PendingUser;
import com.example.SecuroServBackend.Repository.PendingUserRepository;
import com.example.SecuroServBackend.Service.AuthUserService;
import com.example.SecuroServBackend.Service.EmailService;
import com.example.SecuroServBackend.Service.VaultSessionCache;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.method.P;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.crypto.spec.SecretKeySpec;
import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private EmailService emailService;
    @Autowired
    private PendingUserRepository pendingUserRepository;
    @Autowired
    private AuthUserService authUserService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private VaultSessionCache vaultSessionCache;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/createUser")
    public ResponseEntity<?> CreateUser(@RequestBody AuthUserDTO authUserDTO){
        System.out.println("this Otp sender is called");

        if(authUserService.verifyIfEmailPresent(authUserDTO.getEmail()).isPresent()){
            return ResponseEntity.ok("email already there");
        }
        Optional<PendingUser> pendingUser1 = pendingUserRepository.
                findByEmail(authUserDTO.getEmail());
        if(pendingUser1.isPresent()){
            return ResponseEntity.ok("email already exists");
        }
        System.out.println("😒 create user controller is called");
        String otp = OtpGenerator.Generate();
        emailService.sendOtp(authUserDTO.getEmail(),otp);
        PendingUser pendingUser = new PendingUser();
        pendingUser.setId(authUserDTO.getAuthUserID());
        pendingUser.setEmail(authUserDTO.getEmail());
        pendingUser.setOtp(otp);
        pendingUser.setOtpExpiry(LocalDateTime.now().plusMinutes(3));
        pendingUser.setPassword(authUserDTO.getPassword());
        pendingUser.setPassword(passwordEncoder.encode(pendingUser.getPassword()));
        pendingUserRepository.save(pendingUser);
        return ResponseEntity.ok("otp send");
    }

    @DeleteMapping
    public ResponseEntity<?> CancelSignup(@RequestParam String email){
        System.out.println("Cancel signup method has been called");
        pendingUserRepository.deleteByEmail(email);
        return ResponseEntity.ok("");
    }

    @PostMapping("/verification")
    public ResponseEntity<?> Verification(@RequestBody OTP otp, @RequestParam String email){

        System.out.println("verification method is called");
        PendingUser pendingUser = pendingUserRepository.findByEmail(email)
                .orElseThrow(()-> new RuntimeException("Email not entered yet"));
        if(pendingUser.getOtpExpiry().isBefore(LocalDateTime.now())){
            return ResponseEntity.ok("Otp got expired" +
                    "click below resend link to resend again");
        }
        if(!pendingUser.getOtp().equals(otp.getOtp())){
            return ResponseEntity.ok("Otp is invalid");
        }
        AuthUserDTO userDTO = new AuthUserDTO();
        userDTO.setEmail(pendingUser.getEmail());
        System.out.println(pendingUser.getPassword());
        userDTO.setPassword(pendingUser.getPassword());

        AuthUser authUser = authUserService.register(userDTO);

        for(int i = 0; i < 5 ; i++){
            System.out.println("wait for auto login");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        pendingUser.getEmail(),
                        pendingUser.getPassword()
                )
        );

        UserPrinciple principle = (UserPrinciple) authentication.getPrincipal();
        String token = jwtUtil.generateToken(principle);

        System.out.println("token is created");

        SecretKeySpec rootKey = authUserService.decryptRootKey(authUser.getEmail(), pendingUser.getPassword());

        vaultSessionCache.storeRootKey(token, rootKey);

        authUserService.deletePendingUser(pendingUser.getEmail());

        return ResponseEntity.ok(token);
    }

    @PostMapping("/resend")
    public ResponseEntity<?> Resend(@RequestParam String email){
        String OTP = OtpGenerator.Generate();
        PendingUser pendingUser = pendingUserRepository
                .findByEmail(email)
                .orElseThrow(()-> new RuntimeException("Email has not been entered"));
        pendingUser.setOtp(OTP);
        pendingUser.setOtpExpiry(LocalDateTime.now().plusMinutes(3));
        pendingUserRepository.save(pendingUser);
        emailService.sendOtp(pendingUser.getEmail(), pendingUser.getOtp());
        return ResponseEntity.ok("OTP is sent again");
    }
    @PostMapping("/login")
    public ResponseEntity<?> Login(@RequestBody AuthUserDTO authUserDTO){
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(new
                    UsernamePasswordAuthenticationToken(authUserDTO.getEmail(),
                    authUserDTO.getPassword()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.ok("wrong credential");
        }
        UserPrinciple principle = (UserPrinciple) authentication.getPrincipal();

        SecretKeySpec rootKey = authUserService.decryptRootKey(
                authUserDTO.getEmail(),
                authUserDTO.getPassword()
        );


        System.out.println("Decrypted Root Key: " +
                Base64.getEncoder().encodeToString(rootKey.getEncoded()));

        String token = jwtUtil.generateToken(principle);

        vaultSessionCache.storeRootKey(token, rootKey);

        return ResponseEntity.ok(token);
    }
}
