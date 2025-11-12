package uk.jinhy.capstone.api.auth.api.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "현재 사용자 정보 응답")
data class MeResponse(
    @Schema(description = "사용자 ID", example = "1")
    val id: Long,

    @Schema(description = "이름", example = "홍길동")
    val name: String,

    @Schema(description = "이메일", example = "user@example.com")
    val email: String,
)
