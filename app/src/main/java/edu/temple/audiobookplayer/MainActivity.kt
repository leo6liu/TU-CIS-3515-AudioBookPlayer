package edu.temple.audiobookplayer

import android.app.SearchManager
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URL

class MainActivity : AppCompatActivity(), BookListFragment.BookListFragment {
    private var bookDetailsContainer: FragmentContainerView? = null
    private lateinit var bookListFragment: BookListFragment
    private lateinit var bookDetailsFragment: BookDetailsFragment
    private lateinit var bookViewModel: BookViewModel
    private lateinit var bookListViewModel: BookListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // trigger search dialog on button click
        val searchButton = findViewById<Button>(R.id.search)
        searchButton.setOnClickListener {
            onSearchRequested()
        }

        // setup containers, fragments, and view models
        bookDetailsContainer = findViewById(R.id.containerBookDetails)
        bookListFragment = BookListFragment()
        bookDetailsFragment = BookDetailsFragment()
        bookViewModel = ViewModelProvider(this).get(BookViewModel::class.java)
        bookListViewModel = ViewModelProvider(this).get(BookListViewModel::class.java)

        // keep track if book is selected
        val bookSelected = bookViewModel.getBook().value != null

        // determine which fragment(s) should be shown
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

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        // get query from search intent
        if (Intent.ACTION_SEARCH == intent?.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                lifecycleScope.launch(Dispatchers.Main) {
                    search(query)
                }
            }
        }
    }

    // BookListFragment method to display details of selected book
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

    private suspend fun search(query: String) {
        println("[ STATUS ] query received: $query")

        // get books from API
        var responseArray: JSONArray
        withContext(Dispatchers.IO) {
            responseArray = JSONArray(
                URL("https://kamorris.com/lab/cis3515/search.php?term=$query").readText()
            )
        }
        println("[ STATUS ] response received: $responseArray")

        // create list of books from response
        val books = BookList()
        for (i in 0 until responseArray.length()) {
            val book = responseArray.getJSONObject(i)
            books.add(
                Book(
                    book.getString("title"),
                    book.getString("author"),
                    book.getInt("id"),
                    book.getString("cover_url"),
                    book.getInt("duration"),
                )
            )
        }
        println("[ STATUS ] books received: $books")

        // update book list
        bookListViewModel.setList(books)

        // show list fragment and clear book
        val bookSelected = bookViewModel.getBook().value != null
        bookViewModel.clearBook() // this line can be omitted if you don't want to clear the book on search
        if (bookDetailsContainer == null) { // portrait
            if (bookSelected) {
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.containerBookList, bookListFragment)
                    .commit()
            }
        }
    }
}
