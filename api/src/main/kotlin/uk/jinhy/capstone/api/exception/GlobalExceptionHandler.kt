package uk.jinhy.capstone.api.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import uk.jinhy.capstone.util.exception.BadRequestException
import uk.jinhy.capstone.util.exception.BaseException
import uk.jinhy.capstone.util.exception.ConflictException
import uk.jinhy.capstone.util.exception.ForbiddenException
import uk.jinhy.capstone.util.exception.InternalServerException
import uk.jinhy.capstone.util.exception.NotFoundException
import uk.jinhy.capstone.util.exception.UnauthorizedException
import uk.jinhy.capstone.util.response.ApiResponse

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(this::class.java)

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
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(e.code, e.message ?: "Internal Server Error"))
    }

    @ExceptionHandler(BaseException::class)
    fun handleBaseException(e: BaseException): ResponseEntity<ApiResponse<Nothing>> {
        logger.warn("BaseException: {}", e.message, e)
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

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("Unexpected exception: {}", e.message, e)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("INTERNAL_SERVER_ERROR", "An unexpected error occurred"))
    }
}
