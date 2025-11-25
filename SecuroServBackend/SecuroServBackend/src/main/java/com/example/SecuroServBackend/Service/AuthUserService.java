package com.example.SecuroServBackend.Service;

import com.example.SecuroServBackend.Configuration.UserPrinciple;
import com.example.SecuroServBackend.DTOs.AuthUserDTO;
import com.example.SecuroServBackend.Entity.AuthUser;
import com.example.SecuroServBackend.Entity.User;
import com.example.SecuroServBackend.Mappers.AuthUserMapper;
import com.example.SecuroServBackend.Repository.AuthUserRepo;
import com.example.SecuroServBackend.Repository.PendingUserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
@Service
public class AuthUserService implements UserDetailsService {

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthUserRepo authUserRepo;
    @Autowired
    private PendingUserRepository pendingUserRepository;
    @Transactional
    public AuthUser register(AuthUserDTO authUserDTO){
        AuthUser authUser1 = AuthUserMapper.toEntity(authUserDTO);
        String RawPassword = authUser1.getPassword();
        authUser1.setPassword(passwordEncoder.encode(authUser1.getPassword()));
        authUser1.setRole("ROLE_USER");

        try {

            byte[] saltBytes = new byte[16];
            new SecureRandom().nextBytes(saltBytes);
            String saltBase64 = Base64.getEncoder().encodeToString(saltBytes);


            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            PBEKeySpec spec = new PBEKeySpec(RawPassword.toCharArray(), saltBytes, 250_000, 256);
            byte[] umkBytes = factory.generateSecret(spec).getEncoded();
            SecretKeySpec UMK = new SecretKeySpec(umkBytes, "AES");


            byte[] rootKeyBytes = new byte[32];
            new SecureRandom().nextBytes(rootKeyBytes);


            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            byte[] iv = new byte[12];
            new SecureRandom().nextBytes(iv);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);

            cipher.init(Cipher.ENCRYPT_MODE, UMK, gcmSpec);
            byte[] encryptedRootKey = cipher.doFinal(rootKeyBytes);


            byte[] tag = Arrays.copyOfRange(encryptedRootKey, encryptedRootKey.length - 16, encryptedRootKey.length);


            authUser1.setSalt(saltBase64);
            authUser1.setIterations(250000);
            authUser1.setEncryptedRootKeyData(Base64.getEncoder().encodeToString(encryptedRootKey));
            System.out.println(Base64.getEncoder().encodeToString(encryptedRootKey));
            authUser1.setEncryptedRootKeyIv(Base64.getEncoder().encodeToString(iv));
            authUser1.setEncryptedRootKeyTag(Base64.getEncoder().encodeToString(tag));

        } catch (Exception e) {
            throw new RuntimeException("Error initializing vault encryption", e);
        }

        User user = new User();
        user.setAuthUser(authUser1);
        authUser1.setUser(user);

        authUser1 = authUserRepo.save(authUser1);
        return authUser1;
    }

    public Optional<AuthUser> verifyIfEmailPresent(String email){
        return authUserRepo.findByEmail(email);
    }

    @Transactional
    public void deletePendingUser(String email){
        pendingUserRepository.deleteByEmail(email);
    }


    public SecretKeySpec decryptRootKey(String email, String plainPassword) {
        try {

            AuthUser user = authUserRepo.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));


            byte[] saltBytes = Base64.getDecoder().decode(user.getSalt());


            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            PBEKeySpec spec = new PBEKeySpec(
                    plainPassword.toCharArray(),
                    saltBytes,
                    user.getIterations(),
                    256
            );
            byte[] umkBytes = factory.generateSecret(spec).getEncoded();
            SecretKeySpec UMK = new SecretKeySpec(umkBytes, "AES");


            byte[] encryptedRootKeyBytes = Base64.getDecoder().decode(user.getEncryptedRootKeyData());
            byte[] iv = Base64.getDecoder().decode(user.getEncryptedRootKeyIv());


            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcm = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, UMK, gcm);
            byte[] rootKeyBytes = cipher.doFinal(encryptedRootKeyBytes);


            return new SecretKeySpec(rootKeyBytes, "AES");

        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt vault key", e);
        }
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AuthUser authUser = authUserRepo.findByEmail(email)
                .orElseThrow(()->new RuntimeException("email not found"));
        return new UserPrinciple(authUser);
    }
}
