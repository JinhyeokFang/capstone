package uk.jinhy.capstone.api.auth.api.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "현재 사용자 정보 응답")
data class AuthMeResponseDto(
    @Schema(description = "사용자 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    val id: String,

    @Schema(description = "이름", example = "홍길동")
    val name: String,

    @Schema(description = "이메일", example = "user@example.com")
    val email: String,
)
