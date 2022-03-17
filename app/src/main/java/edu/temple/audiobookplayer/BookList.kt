package edu.temple.audiobookplayer

class BookList : Collection<Book> {
    private val books = ArrayList<Book>()

    /**
     * Collection overrides
     */
    override val size: Int
        get() = books.size

    override fun contains(element: Book): Boolean {
        return contains(element)
    }

    override fun containsAll(elements: Collection<Book>): Boolean {
        return books.containsAll(elements)
    }

    override fun isEmpty(): Boolean {
        return books.isEmpty()
    }

    override fun iterator(): Iterator<Book> {
        return books.iterator()
    }

    /**
     * additional operations
     */
    fun add(element: Book) {
        books.add(element)
    }

    fun remove(element: Book) {
        books.remove(element)
    }
    
    fun get(index: Int): Book {
        return books[index]
    }
}