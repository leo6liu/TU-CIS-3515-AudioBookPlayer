package edu.temple.audiobookplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.ViewModelProvider

class MainActivity : AppCompatActivity(), BookListFragment.BookListFragment {
    private var bookDetailsContainer: FragmentContainerView? = null
    private lateinit var bookListFragment: BookListFragment
    private lateinit var bookDetailsFragment: BookDetailsFragment
    private lateinit var bookViewModel: BookViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bookDetailsContainer = findViewById<FragmentContainerView>(R.id.containerBookDetails)
        bookListFragment = BookListFragment.newInstance(BookList.generateBooks())
        bookDetailsFragment = BookDetailsFragment()
        bookViewModel = ViewModelProvider(this).get(BookViewModel::class.java)

        val bookSelected = bookViewModel.getBook().value != null

        // determine which fragments should be shown
        if (savedInstanceState == null) { // first load
            supportFragmentManager
                .beginTransaction()
                .add(R.id.containerBookList, bookListFragment)
                .commit()
        } else {
            if (bookDetailsContainer == null) { // portrait
                if (bookSelected) {
                    // show detail fragment
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.containerBookList, bookDetailsFragment)
                        .commit()
                } else {
                    // show list fragment
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.containerBookList, bookListFragment)
                        .commit()
                }
            } else { // landscape
                // make sure book list shows in containerBookList
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.containerBookList, bookListFragment)
                    .commit()
            }
        }
    }

    override fun bookSelected() {
        if (bookDetailsContainer == null) { // portrait
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.containerBookList, bookDetailsFragment)
                .commit()
        }
    }

    override fun onBackPressed() {
        val bookSelected = bookViewModel.getBook().value != null

        if (bookDetailsContainer == null) { // portrait
            if (bookSelected) {
                bookViewModel.clearBook()
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.containerBookList, bookListFragment)
                    .commit()
            } else {
                this.finish()
            }
        } else { // landscape
            this.finish()
        }
    }
}
