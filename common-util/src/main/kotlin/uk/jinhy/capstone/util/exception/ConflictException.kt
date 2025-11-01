package uk.jinhy.capstone.util.exception

class ConflictException(
    message: String? = "Conflict",
    throwable: Throwable? = null,
) : BaseException(
    code = "CONFLICT",
    message = message,
    throwable = throwable,
)
