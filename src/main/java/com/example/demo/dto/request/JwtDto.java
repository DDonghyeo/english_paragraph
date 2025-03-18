package com.example.demo.dto.request;

import lombok.Builder;

@Builder
public record JwtDto(
        String accessToken,
        String refreshToken
) {
}
