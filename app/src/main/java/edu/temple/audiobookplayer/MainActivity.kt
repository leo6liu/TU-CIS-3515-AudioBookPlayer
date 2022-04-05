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

        // get query from search intent
        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                lifecycleScope.launch(Dispatchers.Main) {
                    search(query)
                }
            }
        }

        // trigger search dialog on button click
        val searchButton = findViewById<Button>(R.id.search)
        searchButton.setOnClickListener {
            onSearchRequested()
        }

        bookDetailsContainer = findViewById<FragmentContainerView>(R.id.containerBookDetails)
        bookListFragment = BookListFragment.newInstance(BookList())
        bookDetailsFragment = BookDetailsFragment()
        bookViewModel = ViewModelProvider(this).get(BookViewModel::class.java)
        bookListViewModel = ViewModelProvider(this).get(BookListViewModel::class.java)

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

    private suspend fun search(query: String) {
        println("[ STATUS ] query received: $query")

        var responseArray: JSONArray
        withContext(Dispatchers.IO) {
            responseArray = JSONArray(
                URL("https://kamorris.com/lab/cis3515/search.php?term=$query").readText()
            )
        }
        println("[ STATUS ] response received: $responseArray")

        val books = BookList()
        for (i in 0 until responseArray.length()) {
            val book = responseArray.getJSONObject(i)
            books.add(
                Book(
                    book.getString("title"),
                    book.getString("author"),
                    book.getInt("id"),
                    book.getString("cover_url"),
                )
            )
        }
        println("[ STATUS ] books received: $books")
    }
}
