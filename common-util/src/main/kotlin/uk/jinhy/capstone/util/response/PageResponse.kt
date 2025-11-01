package uk.jinhy.capstone.util.response

import org.springframework.data.domain.Page

data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val first: Boolean,
    val last: Boolean,
) {
    companion object {
        fun <T> fromPage(page: Page<T>): PageResponse<T> {
            return PageResponse(
                content = page.content,
                page = page.number,
                size = page.size,
                totalElements = page.totalElements,
                totalPages = page.totalPages,
                first = page.isFirst,
                last = page.isLast,
            )
        }

        fun <T, R> fromPage(
            page: Page<T>,
            mapper: (T) -> R,
        ): PageResponse<R> {
            return PageResponse(
                content = page.content.map(mapper),
                page = page.number,
                size = page.size,
                totalElements = page.totalElements,
                totalPages = page.totalPages,
                first = page.isFirst,
                last = page.isLast,
            )
        }

        fun <T> of(
            content: List<T>,
            page: Int,
            size: Int,
            totalElements: Long,
        ): PageResponse<T> {
            val totalPages = if (size > 0) {
                (totalElements + size - 1) / size
            } else {
                0
            }
            return PageResponse(
                content = content,
                page = page,
                size = size,
                totalElements = totalElements,
                totalPages = totalPages.toInt(),
                first = page == 0,
                last = page >= totalPages - 1,
            )
        }
    }
}
