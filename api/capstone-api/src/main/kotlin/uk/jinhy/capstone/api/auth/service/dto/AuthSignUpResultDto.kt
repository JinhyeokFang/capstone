package uk.jinhy.capstone.api.auth.service.dto

data class AuthSignUpResultDto(
    val accessToken: String,
    val refreshToken: String,
)
