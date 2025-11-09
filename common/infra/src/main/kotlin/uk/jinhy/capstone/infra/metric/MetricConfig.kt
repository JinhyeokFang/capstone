package uk.jinhy.capstone.infra.metric

import io.micrometer.core.aop.TimedAspect
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

@Configuration
class MetricConfig(
    private val meterRegistry: MeterRegistry,
    private val threadPoolTaskExecutor: ThreadPoolTaskExecutor,
) {

    companion object {
        const val THREAD_POOL_QUEUE_SIZE_GAUGE = "thread_pool_queue_size"
        const val THREAD_POOL_ACTIVE_COUNT_GAUGE = "thread_pool_active_count"

        const val HTTP_REQUESTS_COUNTER = "http_requests"
        const val HTTP_REQUEST_ELAPSED_TIME_TIMER = "http_request_elapsed_time"
    }

    @PostConstruct
    fun initMetrics() {
        Gauge.builder(THREAD_POOL_QUEUE_SIZE_GAUGE) { threadPoolTaskExecutor.queueSize }
            .register(meterRegistry)
        Gauge.builder(THREAD_POOL_ACTIVE_COUNT_GAUGE) { threadPoolTaskExecutor.activeCount }
            .register(meterRegistry)

        Counter.builder(HTTP_REQUESTS_COUNTER)
            .register(meterRegistry)
        Timer.builder(HTTP_REQUEST_ELAPSED_TIME_TIMER)
            .register(meterRegistry)
    }

    @Bean
    fun timedAspect(): TimedAspect {
        return TimedAspect(meterRegistry)
    }
}
