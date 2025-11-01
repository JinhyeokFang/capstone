package uk.jinhy.capstone.util

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

object DateUtil {
    private val DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun format(localDateTime: LocalDateTime, pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
        return localDateTime.format(DateTimeFormatter.ofPattern(pattern))
    }

    fun toDate(localDateTime: LocalDateTime): Date {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant())
    }

    fun toLocalDateTime(date: Date): LocalDateTime {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
    }

    fun now(): LocalDateTime {
        return LocalDateTime.now()
    }
}
