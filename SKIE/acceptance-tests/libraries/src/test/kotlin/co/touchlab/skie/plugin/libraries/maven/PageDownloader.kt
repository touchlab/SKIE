package co.touchlab.skie.plugin.libraries.maven

class PageDownloader<T>(private val pageSize: Int) {

    suspend fun getPages(fromPage: Int, numberOfPages: Int, getPage: suspend (PageIndex) -> Pair<List<T>, ItemCount>): List<T> {
        val (firstPage, itemCount) = getPage(PageIndex(fromPage))

        val numberOfExistingPages = itemCount.getNumberOfPages(pageSize)

        val lastPageNumberExcluding = numberOfExistingPages.coerceAtMost(fromPage + numberOfPages)

        println("Querying from $fromPage to $lastPageNumberExcluding (excluding) of total $numberOfExistingPages pages")

        val remainingPages = ((fromPage + 1) until lastPageNumberExcluding).flatMap { getPage(PageIndex(it)).first }

        return firstPage + remainingPages
    }

    @JvmInline
    value class PageIndex(val index: Int)

    sealed interface ItemCount {

        fun getNumberOfPages(pageSize: Int): Int

        data class Pages(val count: Int) : ItemCount {

            override fun getNumberOfPages(pageSize: Int): Int = count
        }

        data class Items(val count: Int) : ItemCount {

            override fun getNumberOfPages(pageSize: Int): Int =
                if (count % pageSize == 0) {
                    count / pageSize
                } else {
                    (count / pageSize) + 1
                }
        }
    }
}
