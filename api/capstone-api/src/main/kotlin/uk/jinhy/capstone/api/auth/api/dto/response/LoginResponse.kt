package uk.jinhy.capstone.api.auth.api.dto.response

// Temporarily commented out Swagger imports due to dependency issues
// import io.swagger.v3.oas.annotations.media.Schema

// Temporarily commented out Swagger annotations due to dependency issues
// @Schema(description = "로그인 응답")
data class LoginResponse(
    // Temporarily commented out Swagger annotations due to dependency issues
    // @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    val accessToken: String,
)
