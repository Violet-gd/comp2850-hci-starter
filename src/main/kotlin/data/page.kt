package data

/**
 * Generic pagination container.
 * Wraps a list of items with metadata for page navigation.
 */
data class Page<T>(
    val items: List<T>,
    val page: Int,
    val pages: Int,
    val total: Int
) {
    val hasPrevious: Boolean get() = page > 1
    val hasNext: Boolean get() = page < pages
    val previousPage: Int get() = page - 1
    val nextPage: Int get() = page + 1
}
