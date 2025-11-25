package com.example.SecuroServBackend.Controllers;

import com.example.SecuroServBackend.Configuration.JwtUtil;
import com.example.SecuroServBackend.Entity.Role;
import com.example.SecuroServBackend.Entity.User;
import com.example.SecuroServBackend.Repository.UserRepo;
import com.razorpay.RazorpayClient;
import com.razorpay.Order;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Optional;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Value("rzp_test_RcroVaZbZlLpxT")
    private String razorpayKeyId;

    @Value("88yD9jUkynLSw0FIj6JzL9M5")
    private String razorpayKeySecret;

    private final UserRepo userRepo;
    private final JwtUtil jwtUtil;

    public PaymentController(UserRepo userRepo, JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.jwtUtil = jwtUtil;
    }


    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestHeader("Authorization") String token) {
        try {
            RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", 270000); // ₹2700
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "order_rcpt_" + System.currentTimeMillis());

            Order order = razorpay.orders.create(orderRequest);
            return ResponseEntity.ok(order.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error creating order: " + e.getMessage());
        }
    }


    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(
            @RequestHeader("Authorization") String token,
            @RequestBody String body
    ) {
        try {
            JSONObject payload = new JSONObject(body);
            String orderId = payload.getString("razorpay_order_id");
            String paymentId = payload.getString("razorpay_payment_id");
            String signature = payload.getString("razorpay_signature");


            String data = orderId + "|" + paymentId;
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(razorpayKeySecret.getBytes(), "HmacSHA256");
            sha256Hmac.init(secretKey);

            byte[] hashBytes = sha256Hmac.doFinal(data.getBytes());
            StringBuilder hash = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hash.append('0');
                hash.append(hex);
            }
            String expectedSignature = hash.toString();

            if (!expectedSignature.equals(signature)) {
                System.out.println("❌ Signature mismatch:");
                System.out.println("Expected: " + expectedSignature);
                System.out.println("Received: " + signature);
                return ResponseEntity.status(400).body("Payment verification failed: Invalid signature.");
            }


            String email = jwtUtil.extractUsername(token.replace("Bearer ", ""));
            Optional<User> userOpt = userRepo.findByAuthUser_Email(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body("User not found.");
            }


            User user = userOpt.get();
            user.setRole(Role.PREMIUM);
            userRepo.save(user);

            System.out.println("✅ Payment verified and upgraded user: " + email);
            return ResponseEntity.ok("Payment verified successfully. You are now a PREMIUM user!");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Verification failed: " + e.getMessage());
        }
    }
}
