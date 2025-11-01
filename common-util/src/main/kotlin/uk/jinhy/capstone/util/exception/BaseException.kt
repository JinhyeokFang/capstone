package uk.jinhy.capstone.util.exception

abstract class BaseException(
    val code: String,
    message: String? = null,
    throwable: Throwable? = null,
) : RuntimeException(message, throwable)
