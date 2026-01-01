package uk.jinhy.capstone.util.exception

class NotFoundException(
    message: String? = "Not Found",
    code: String = "NOT_FOUND",
    throwable: Throwable? = null,
) : BaseException(
    code = code,
    message = message,
    throwable = throwable,
)
