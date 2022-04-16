package edu.temple.audiobookplayer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NowPlayingTextViewModel: ViewModel() {
    private val text: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun setText(text: String) {
        this.text.value = text
    }

    fun getText(): LiveData<String> {
        return text
    }
}