package edu.temple.audiobookplayer

import android.os.Parcel
import android.os.Parcelable

data class BookList(
    var books: ArrayList<Book> = ArrayList(),
) : Parcelable {
    constructor(parcel: Parcel) : this(
        // this works
        arrayListOf<Book>().apply {
            parcel.readArrayList(Book::class.java.classLoader)
        }
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(books)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BookList> {
        override fun createFromParcel(parcel: Parcel): BookList {
            return BookList(parcel)
        }

        override fun newArray(size: Int): Array<BookList?> {
            return arrayOfNulls(size)
        }

        /**
         * Generates a BookList with a hard coded set of Book objects.

        fun generateBooks(): BookList {
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
         */
    }

    /**
     * collection-like operations
     */

    val size: Int
        get() = books.size

    fun add(element: Book) {
        books.add(element)
    }

    fun remove(element: Book) {
        books.remove(element)
    }

    operator fun get(index: Int): Book {
        return books[index]
    }
}
