package uk.jinhy.capstone.util.test.fixture

import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

object TestDataGenerator {
    fun generateId(): Long {
        return System.currentTimeMillis() % Long.MAX_VALUE
    }

    fun generateUuid(): String {
        return UUID.randomUUID().toString()
    }

    fun generateInstant(offsetSeconds: Long = 0): Instant {
        return Instant.now().plusSeconds(offsetSeconds)
    }

    fun generateLocalDateTime(offsetDays: Int = 0): LocalDateTime {
        return LocalDateTime.now().plusDays(offsetDays.toLong())
    }

    fun generateRandomNumber(min: Int = 0, max: Int = 100): Int {
        return (min..max).random()
    }

    fun generateRandomLong(min: Long = 0L, max: Long = 1000L): Long {
        return (min..max).random()
    }

    fun generateBoolean(): Boolean {
        return listOf(true, false).random()
    }
}
