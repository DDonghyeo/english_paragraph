package com.example.demo.dto.response;

import com.example.demo.entity.User;
import lombok.Builder;

@Builder
public record UserResponseDto(
        String name,

        String email

) {
    public static UserResponseDto from(User user) {
        return UserResponseDto.builder()
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}
