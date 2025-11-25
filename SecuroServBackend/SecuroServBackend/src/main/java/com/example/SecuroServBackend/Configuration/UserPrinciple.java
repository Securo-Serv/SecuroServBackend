package com.example.SecuroServBackend.Configuration;

import com.example.SecuroServBackend.Entity.AuthUser;
import com.example.SecuroServBackend.Entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserPrinciple implements UserDetails {
    private final AuthUser authUser;
    public UserPrinciple(AuthUser authUser){
        this.authUser = authUser;
    }

    public AuthUser returnObject(){
        return authUser;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return authUser.getPassword();
    }

    @Override
    public String getUsername() {
        return authUser.getEmail();
    }
}
