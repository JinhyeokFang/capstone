package uk.jinhy.capstone.util.exception

class BadRequestException(
    message: String? = "Bad Request",
    code: String = "BAD_REQUEST",
    throwable: Throwable? = null,
) : BaseException(
    code = code,
    message = message,
    throwable = throwable,
)
