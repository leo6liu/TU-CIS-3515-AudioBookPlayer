package edu.temple.audiobookplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val books = BookList.generateBooks()

        val bookListFragment = BookListFragment.newInstance(books)

        supportFragmentManager.beginTransaction()
            .add(R.id.containerBookList, bookListFragment)
            .commit()
    }
}
