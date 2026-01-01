package uk.jinhy.capstone.util.logging

import com.fasterxml.jackson.databind.ObjectMapper

class Logger(
    private val logger: org.slf4j.Logger,
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
