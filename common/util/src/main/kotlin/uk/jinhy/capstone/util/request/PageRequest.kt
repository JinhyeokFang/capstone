package uk.jinhy.capstone.util.request

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.PageRequest as SpringPageRequest

data class PageRequest(
    @field:Min(0)
    val page: Int = 0,
    @field:Min(1)
    @field:Max(1000)
    val size: Int = 20,
) {
    fun toSpringPageable(): Pageable {
        return SpringPageRequest.of(page, size)
    }
}
