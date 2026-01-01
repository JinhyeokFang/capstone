package uk.jinhy.capstone.api.auth.service.dto

data class AuthSignUpDto(
    val name: String,
    val email: String,
    val password: String,
)
