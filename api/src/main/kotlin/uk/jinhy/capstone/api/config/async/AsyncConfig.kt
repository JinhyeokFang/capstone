package uk.jinhy.capstone.api.config.async

import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskDecorator
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import uk.jinhy.capstone.util.exception.BaseException
import java.util.concurrent.Executor

@EnableAsync
@Configuration
class AsyncConfig(
    @Value("\${capstone.async.core-pool-size:128}") private val corePoolSize: Int,
    @Value("\${capstone.async.max-pool-size:128}") private val maxPoolSize: Int,
    @Value("\${capstone.async.queue-capacity:12800}") private val queueCapacity: Int,
    @Value("\${capstone.async.await-termination-seconds:10}") private val awaitTerminationSeconds: Int,
) : AsyncConfigurer {

    private val logger = LoggerFactory.getLogger(AsyncConfig::class.java)

    override fun getAsyncExecutor(): Executor? {
        return threadPoolTaskExecutor()
    }

    override fun getAsyncUncaughtExceptionHandler(): AsyncUncaughtExceptionHandler? {
        return AsyncUncaughtExceptionHandler { exception, method, parameters ->
            MDC.put("method", method.name)
            MDC.put("parameters", parameters.joinToString(","))

            when (exception) {
                is BaseException -> logger.warn("BaseException in async method: {}", exception.message, exception)
                else -> logger.error("Exception in async method: {}", exception.message, exception)
            }

            MDC.clear()
        }
    }

    @Bean
    fun threadPoolTaskExecutor(): ThreadPoolTaskExecutor {
        val executor = ThreadPoolTaskExecutor()
        executor.setThreadNamePrefix("async-job-")
        executor.corePoolSize = corePoolSize
        executor.maxPoolSize = maxPoolSize
        executor.queueCapacity = queueCapacity
        executor.setTaskDecorator(CopyingMdcTaskDecorator())
        executor.setWaitForTasksToCompleteOnShutdown(true)
        executor.setAwaitTerminationSeconds(awaitTerminationSeconds)

        return executor
    }
}

class CopyingMdcTaskDecorator : TaskDecorator {
    override fun decorate(runnable: Runnable): Runnable {
        val contextMap = MDC.getCopyOfContextMap()
        return Runnable {
            contextMap?.let { MDC.setContextMap(it) }
            try {
                runnable.run()
            } finally {
                MDC.clear()
            }
        }
    }
}
