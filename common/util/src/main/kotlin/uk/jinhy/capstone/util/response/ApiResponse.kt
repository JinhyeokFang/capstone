package uk.jinhy.capstone.util.response

data class ApiResponse<T>(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: T? = null,
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> {
            return ApiResponse(
                success = true,
                code = "SUCCESS",
                message = "Success",
                data = data,
            )
        }

        fun <T> success(message: String, data: T? = null): ApiResponse<T> {
            return ApiResponse(
                success = true,
                code = "SUCCESS",
                message = message,
                data = data,
            )
        }

        fun error(code: String, message: String): ApiResponse<Nothing> {
            return ApiResponse(
                success = false,
                code = code,
                message = message,
                data = null,
            )
        }
    }
}
