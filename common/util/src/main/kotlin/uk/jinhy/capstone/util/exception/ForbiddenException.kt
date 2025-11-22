package uk.jinhy.capstone.util.exception

class ForbiddenException(
    message: String? = "Forbidden",
    throwable: Throwable? = null,
) : BaseException(
    code = "FORBIDDEN",
    message = message,
    throwable = throwable,
)
