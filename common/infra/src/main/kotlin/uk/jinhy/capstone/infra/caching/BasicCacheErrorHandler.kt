package uk.jinhy.capstone.infra.caching

import org.slf4j.LoggerFactory
import org.springframework.cache.Cache
import org.springframework.cache.interceptor.CacheErrorHandler
import org.springframework.stereotype.Component

@Component
class BasicCacheErrorHandler : CacheErrorHandler {

    private val logger = LoggerFactory.getLogger(BasicCacheErrorHandler::class.java)

    override fun handleCacheGetError(
        exception: RuntimeException,
        cache: Cache,
        key: Any,
    ) {
        logger.warn("Unable to get cache for '$key'.", exception)
    }

    override fun handleCachePutError(
        exception: RuntimeException,
        cache: Cache,
        key: Any,
        value: Any?,
    ) {
        logger.warn("Unable to put cache for '$key'.", exception)
    }

    override fun handleCacheEvictError(
        exception: RuntimeException,
        cache: Cache,
        key: Any,
    ) {
        logger.warn("Unable to evict cache for '$key'.", exception)
    }

    override fun handleCacheClearError(
        exception: RuntimeException,
        cache: Cache,
    ) {
        logger.warn("Unable to clear cache.", exception)
    }
}
