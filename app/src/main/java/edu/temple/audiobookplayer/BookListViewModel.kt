package edu.temple.audiobookplayer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class BookListViewModel : ViewModel() {
    private val bookList: MutableLiveData<BookList> by lazy {
        MutableLiveData<BookList>()
    }

    fun setList(bookList: BookList) {
        this.bookList.postValue(bookList)
    }

    fun getList(): LiveData<BookList> {
        return bookList
    }
}