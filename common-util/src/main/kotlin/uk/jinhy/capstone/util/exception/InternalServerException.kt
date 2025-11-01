package uk.jinhy.capstone.util.exception

class InternalServerException(
    message: String? = "Internal Server Error",
    throwable: Throwable? = null,
) : BaseException(
    code = "INTERNAL_SERVER_ERROR",
    message = message,
    throwable = throwable,
)
