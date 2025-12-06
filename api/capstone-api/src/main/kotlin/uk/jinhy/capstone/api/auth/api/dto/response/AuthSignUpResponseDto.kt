package uk.jinhy.capstone.api.auth.api.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "회원가입 응답")
data class AuthSignUpResponseDto(
    @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    val accessToken: String,
)
