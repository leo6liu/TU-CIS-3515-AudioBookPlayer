package edu.temple.audiobookplayer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BookViewModel: ViewModel() {
    private val selectedBook: MutableLiveData<Book> by lazy {
        MutableLiveData<Book>()
    }

    fun setBook(book: Book) {
        selectedBook.value = book
    }

    fun clearBook() {
        selectedBook.value = null
    }

    fun getBook(): LiveData<Book> {
        return selectedBook
    }
}