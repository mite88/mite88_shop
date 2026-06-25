package io.mite88.mite88shop.members.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {}
