package com.example.SecuroServBackend.Configuration;

import java.util.Random;

public class OtpGenerator {
    public static String Generate(){
        StringBuilder otp = new StringBuilder();
        Random random = new Random();
        for(int i = 1; i<=6; i++){
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
    public static String generateOtp4() {
        Random random = new Random();
        int otp = 1000 + random.nextInt(9000);
        return String.valueOf(otp);
    }
}
