package com.example.SecuroServBackend.Configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(
                                "http://localhost:5173",
                                "https://lucille-unbatted-monica.ngrok-free.dev",
                                "https://securoserv.co.in",
                                "https://www.securoserv.co.in"
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")


                        .allowedHeaders(

                                "Authorization",
                                "Content-Type",
                                "Accept",
                                "X-Auth-Token",
                                "X-Requested-With",
                                "Origin",
                                "X-Session-Token"
                        )
                        .exposedHeaders("Authorization")


                        .allowCredentials(true);


            }
        };
    }
}
