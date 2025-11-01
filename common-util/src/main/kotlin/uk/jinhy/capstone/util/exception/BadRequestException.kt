package uk.jinhy.capstone.util.exception

class BadRequestException(
    message: String? = "Bad Request",
    throwable: Throwable? = null,
) : BaseException(
    code = "BAD_REQUEST",
    message = message,
    throwable = throwable,
)
