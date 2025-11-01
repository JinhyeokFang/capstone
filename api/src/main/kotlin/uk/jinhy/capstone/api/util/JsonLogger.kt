package uk.jinhy.capstone.api.util

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger

class JsonLogger(
    private val logger: Logger,
    private val objectMapper: ObjectMapper,
) {
    fun info(
        type: String,
        title: String,
        message: String,
        elapsedTimeMillis: Long,
        data: Any? = null,
    ) {
        val logMap = mapOf(
            "type" to type,
            "title" to title,
            "message" to message,
            "elapsedTimeMillis" to elapsedTimeMillis,
            "data" to objectMapper.writeValueAsString(data),
        )
        logger.info(objectMapper.writeValueAsString(logMap))
    }

    fun error(
        type: String,
        title: String,
        message: String,
        elapsedTimeMillis: Long,
        data: Any? = null,
        throwable: Throwable? = null,
    ) {
        val logMap = mutableMapOf<String, Any>(
            "type" to type,
            "title" to title,
            "message" to message,
            "elapsedTimeMillis" to elapsedTimeMillis,
            "data" to objectMapper.writeValueAsString(data),
        )
        if (throwable != null) {
            logMap["throwable"] = throwable.message ?: ""
        }
        logger.error(objectMapper.writeValueAsString(logMap), throwable)
    }
}
