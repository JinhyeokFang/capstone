package uk.jinhy.capstone.util.exception

class ConflictException(
    message: String? = "Conflict",
    code: String = "CONFLICT",
    throwable: Throwable? = null,
) : BaseException(
    code = code,
    message = message,
    throwable = throwable,
)
