package uk.jinhy.capstone.util.async

import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

@Component
class AsyncUtil {

    @Async
    fun execute(function: () -> Unit) {
        return function()
    }

    @Async
    fun <T> executeAsync(function: () -> T): CompletableFuture<T> {
        return CompletableFuture.supplyAsync(function)
    }
}
