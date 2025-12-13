package uk.jinhy.capstone.api.auth.service.dto

data class AuthLoginResultDto(
    val accessToken: String,
    val refreshToken: String,
)
