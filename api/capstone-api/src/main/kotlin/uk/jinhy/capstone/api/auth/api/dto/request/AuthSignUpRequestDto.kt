package uk.jinhy.capstone.api.auth.api.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "회원가입 요청")
data class AuthSignUpRequestDto(
    @field:NotBlank(message = "이름은 필수입니다.")
    @field:Size(min = 1, max = 255, message = "이름은 1자 이상 255자 이하여야 합니다.")
    @Schema(description = "이름", example = "홍길동")
    val name: String,

    @field:NotBlank(message = "이메일은 필수입니다.")
    @field:Email(message = "유효한 이메일 형식이 아닙니다.")
    @Schema(description = "이메일", example = "user@example.com")
    val email: String,

    @field:NotBlank(message = "비밀번호는 필수입니다.")
    @field:Size(min = 8, max = 255, message = "비밀번호는 8자 이상 255자 이하여야 합니다.")
    @Schema(description = "비밀번호", example = "password123")
    val password: String,
)
