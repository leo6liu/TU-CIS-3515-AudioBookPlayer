package edu.temple.audiobookplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentContainerView

class MainActivity : AppCompatActivity(), BookListFragment.BookListFragment {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager
            .beginTransaction()
            .add(R.id.containerBookList, BookListFragment.newInstance(BookList.generateBooks()))
            .commit()
    }

    override fun bookSelected() {
        if (findViewById<FragmentContainerView>(R.id.containerBookDetails) == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.containerBookList, BookDetailsFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}
