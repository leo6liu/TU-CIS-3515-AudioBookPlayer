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

    companion object {
        /**
         * Generates a BookList with a hard coded set of Book objects.
         */
        fun generateList(): BookList {
            val books = BookList()

            books.add(Book("Steve Jobs", "Walter Isaacson"))
            books.add(Book("Harry Potter and the Philosopher's Stone", "J.K. Rowling"))
            books.add(Book("Harry Potter and the Chamber of Secrets", "J.K. Rowling"))
            books.add(Book("Harry Potter and the Prisoner of Azkaban", "J.K. Rowling"))
            books.add(Book("Harry Potter and the Goblet of Fire", "J.K. Rowling"))
            books.add(Book("Harry Potter and the Order of Phoenix", "J.K. Rowling"))
            books.add(Book("Harry Potter and the Half-Blood Prince", "J.K. Rowling"))
            books.add(Book("Harry Potter and the Deathly Hallows", "J.K. Rowling"))
            books.add(Book("The Lightning Thief", "Rick Riordan"))
            books.add(Book("The Sea of Monsters", "Rick Riordan"))
            books.add(Book("The Titan’s Curse", "Rick Riordan"))
            books.add(Book("The Battle of the Labyrinth", "Rick Riordan"))
            books.add(Book("The Last Olympian", "Rick Riordan"))
            books.add(Book("The Lost Hero", "Rick Riordan"))
            books.add(Book("The Son of Neptune", "Rick Riordan"))
            books.add(Book("The Mark of Athena", "Rick Riordan"))
            books.add(Book("The House of Hades", "Rick Riordan"))
            books.add(Book("The Blood of Olympus", "Rick Riordan"))

            return books
        }
    }
}