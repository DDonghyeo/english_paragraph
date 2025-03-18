package com.example.demo.dto.request;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import lombok.Builder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

public class UserRequest {

    @Builder
    public record UpdateDto(
           String name

    ){}

    @Builder
    public record SignUpDto(
            String name,
            String email,
            String password
    ){
        public User toEntity(PasswordEncoder passwordEncoder) {
            return User.builder()
                    .name(name)
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .roles(Collections.singletonList(Role.ROLE_USER))
                    .build();
        }
    }
}
