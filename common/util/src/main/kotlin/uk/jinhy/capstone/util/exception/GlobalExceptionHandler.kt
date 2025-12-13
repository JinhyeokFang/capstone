package uk.jinhy.capstone.util.exception

import io.sentry.Sentry
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.resource.NoResourceFoundException
import uk.jinhy.capstone.util.response.ApiResponse

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(this::class.java)

    private fun captureExceptionToSentry(exception: Exception, level: SentryLevel = SentryLevel.ERROR) {
        try {
            val sentryKey = System.getenv("SENTRY_KEY")
            if (sentryKey.isNullOrBlank()) {
                return
            }

            Sentry.configureScope { scope ->
                scope.setTag("exception.type", exception.javaClass.simpleName)
                scope.setTag("exception.message", exception.message ?: "")
                scope.setContexts(
                    "exception",
                    mapOf(
                        "type" to exception.javaClass.name,
                        "message" to (exception.message ?: ""),
                    ),
                )
            }

            when (level) {
                SentryLevel.ERROR -> Sentry.captureException(exception)
                SentryLevel.WARNING -> {
                    Sentry.captureException(exception) { it.level = io.sentry.SentryLevel.WARNING }
                }
                SentryLevel.INFO -> {
                    Sentry.captureException(exception) { it.level = io.sentry.SentryLevel.INFO }
                }
            }
        } catch (e: Exception) {
            logger.debug("Failed to capture exception to Sentry: {}", e.message)
        }
    }

    private enum class SentryLevel {
        ERROR, WARNING, INFO
    }

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequestException(e: BadRequestException): ResponseEntity<ApiResponse<Nothing>> {
        logger.debug("BadRequestException: {}", e.message, e)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(e.code, e.message ?: "Bad Request"))
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(e: NotFoundException): ResponseEntity<ApiResponse<Nothing>> {
        logger.debug("NotFoundException: {}", e.message, e)
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(e.code, e.message ?: "Not Found"))
    }

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFoundException(e: NoResourceFoundException): ResponseEntity<ApiResponse<Nothing>> {
        logger.debug("NoResourceFoundException: {}", e.message, e)
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error("NOT_FOUND", e.message ?: "Resource not found"))
    }

    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorizedException(e: UnauthorizedException): ResponseEntity<ApiResponse<Nothing>> {
        logger.debug("UnauthorizedException: {}", e.message, e)
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error(e.code, e.message ?: "Unauthorized"))
    }

    @ExceptionHandler(ForbiddenException::class)
    fun handleForbiddenException(e: ForbiddenException): ResponseEntity<ApiResponse<Nothing>> {
        logger.debug("ForbiddenException: {}", e.message, e)
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.error(e.code, e.message ?: "Forbidden"))
    }

    @ExceptionHandler(ConflictException::class)
    fun handleConflictException(e: ConflictException): ResponseEntity<ApiResponse<Nothing>> {
        logger.debug("ConflictException: {}", e.message, e)
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ApiResponse.error(e.code, e.message ?: "Conflict"))
    }

    @ExceptionHandler(InternalServerException::class)
    fun handleInternalServerException(e: InternalServerException): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("InternalServerException: {}", e.message, e)
        captureExceptionToSentry(e, SentryLevel.ERROR)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(e.code, e.message ?: "Internal Server Error"))
    }

    @ExceptionHandler(BaseException::class)
    fun handleBaseException(e: BaseException): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("BaseException: {}", e.message, e)
        captureExceptionToSentry(e, SentryLevel.WARNING)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(e.code, e.message ?: "Unknown Error"))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(
        e: MethodArgumentNotValidException,
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.debug("Validation error: {}", e.message)
        val errors = e.bindingResult.fieldErrors
            .associate { it.field to (it.defaultMessage ?: "Invalid value") }
        val errorMessage = errors.entries.joinToString(", ") { "${it.key}: ${it.value}" }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("VALIDATION_ERROR", errorMessage))
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(
        e: HttpMessageNotReadableException,
    ): ResponseEntity<ApiResponse<Nothing>> {
        logger.debug("HTTP message not readable: {}", e.message)
        val message = e.message?.let {
            when {
                it.contains("missing") || it.contains("NULL") -> "필수 필드가 누락되었습니다."
                else -> "요청 본문을 읽을 수 없습니다."
            }
        } ?: "요청 본문을 읽을 수 없습니다."

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("BAD_REQUEST", message))
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("Unexpected exception: {}", e.message, e)
        captureExceptionToSentry(e, SentryLevel.ERROR)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("INTERNAL_SERVER_ERROR", "An unexpected error occurred"))
    }
}
