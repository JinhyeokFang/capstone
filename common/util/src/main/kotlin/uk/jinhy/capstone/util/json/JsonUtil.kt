package uk.jinhy.capstone.util.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

object JsonUtil {
    val objectMapper: ObjectMapper = jacksonObjectMapper()
        .registerModule(JavaTimeModule())

    fun toJson(obj: Any): String {
        return objectMapper.writeValueAsString(obj)
    }

    inline fun <reified T> fromJson(json: String): T {
        return objectMapper.readValue(json)
    }

    fun <T> fromJson(json: String, clazz: Class<T>): T {
        return objectMapper.readValue(json, clazz)
    }

    fun toPrettyJson(obj: Any): String {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj)
    }
}
