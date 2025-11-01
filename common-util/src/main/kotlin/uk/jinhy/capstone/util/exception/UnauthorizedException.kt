package uk.jinhy.capstone.util.exception

class UnauthorizedException(
    message: String? = "Unauthorized",
    throwable: Throwable? = null,
) : BaseException(
    code = "UNAUTHORIZED",
    message = message,
    throwable = throwable,
)
