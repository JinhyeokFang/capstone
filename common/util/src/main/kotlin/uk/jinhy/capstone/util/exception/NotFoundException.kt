package uk.jinhy.capstone.util.exception

class NotFoundException(
    message: String? = "Not Found",
    throwable: Throwable? = null,
) : BaseException(
    code = "NOT_FOUND",
    message = message,
    throwable = throwable,
)
