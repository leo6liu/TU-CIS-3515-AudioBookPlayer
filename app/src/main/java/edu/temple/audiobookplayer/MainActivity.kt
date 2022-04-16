package edu.temple.audiobookplayer

import android.app.SearchManager
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Button
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import edu.temple.audlibplayer.PlayerService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URL

class MainActivity : AppCompatActivity(), BookListFragment.BookListFragment,
    BookDetailsFragment.BookDetailsFragment, ControlsFragment.ControlsFragment {
    private var bookDetailsContainer: FragmentContainerView? = null

    // fragments
    private lateinit var bookListFragment: BookListFragment
    private lateinit var bookDetailsFragment: BookDetailsFragment
    private lateinit var controlsFragment: ControlsFragment

    // viewmodels
    private lateinit var bookViewModel: BookViewModel
    private lateinit var bookListViewModel: BookListViewModel
    private lateinit var nowPlayingTextViewModel: NowPlayingTextViewModel

    // service
    var isConnected = false
    lateinit var mediaControlBinder: PlayerService.MediaControlBinder
    val progressHandler = Handler(Looper.getMainLooper()) {
        if (isConnected) {
            it.obj?.let { obj ->
                val progress = (obj as PlayerService.BookProgress).progress
                controlsFragment.updateProgress(progress)
            }
        }
        return@Handler true
    }
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            isConnected = true
            mediaControlBinder = service as PlayerService.MediaControlBinder
            mediaControlBinder.setProgressHandler(progressHandler)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isConnected = false
        }
    }

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
        controlsFragment = ControlsFragment()
        bookListFragment = BookListFragment()
        bookDetailsFragment = BookDetailsFragment()
        bookViewModel = ViewModelProvider(this).get(BookViewModel::class.java)
        bookListViewModel = ViewModelProvider(this).get(BookListViewModel::class.java)
        nowPlayingTextViewModel = ViewModelProvider(this).get(NowPlayingTextViewModel::class.java)

        // start and bind to service
        bindService(Intent(this, PlayerService::class.java), serviceConnection, BIND_AUTO_CREATE)

        // determine which fragment(s) should be shown
        if (savedInstanceState == null) { // first load
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.containerBookList, bookListFragment)
                .replace(R.id.containerControls, controlsFragment)
                .commit()
        } else {
            if (bookDetailsContainer == null) { // portrait
                if (bookViewModel.getBook().value != null) {
                    // show detail fragment
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.containerBookList, bookDetailsFragment)
                        .replace(R.id.containerControls, controlsFragment)
                        .commit()
                } else {
                    // show list fragment
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.containerBookList, bookListFragment)
                        .replace(R.id.containerControls, controlsFragment)
                        .commit()
                }
            } else { // landscape
                // make sure book list shows in containerBookList
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.containerBookList, bookListFragment)
                    .replace(R.id.containerControls, controlsFragment)
                    .commit()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        if (isConnected && mediaControlBinder.isPlaying && bookDetailsContainer == null && bookViewModel.getBook().value != null) {
            // show detail fragment
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.containerBookList, bookDetailsFragment)
                .replace(R.id.containerControls, controlsFragment)
                .commit()
        }
    }

    // called when user performs a search
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
    override fun bookSelected(book: Book) {
        // start playing selected book
        // UNCOMMENT TO AUTOPLAY BOOK WHENEVER A NEW ONE IS SELECTED!!!
//        if (isConnected) {
//            mediaControlBinder.play(book.id)
//            nowPlayingTextViewModel.setText("Now Playing: ${book.title}")
//        }

        if (bookDetailsContainer == null) { // portrait
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.containerBookList, bookDetailsFragment)
                .commit()
        }
    }

    // ControlsFragment method which triggers on Play clicked
    override fun onPlayClicked() {
        if (isConnected && !mediaControlBinder.isPlaying) {
            mediaControlBinder.pause()
            bookViewModel.getBook().value?.let { book ->
                if (mediaControlBinder.isPlaying) {
                    nowPlayingTextViewModel.setText("Now Playing: ${book.title}")
                } else {
                    mediaControlBinder.play(book.id)
                    nowPlayingTextViewModel.setText("Now Playing: ${book.title}")
                }
            }
        }
    }

    // ControlsFragment method which triggers on Pause clicked
    override fun onPauseClicked() {
        if (isConnected && mediaControlBinder.isPlaying) {
            mediaControlBinder.pause()
            bookViewModel.getBook().value?.let { book ->
                nowPlayingTextViewModel.setText("Paused: ${book.title}")
            }
        }
    }

    // ControlsFragment method which triggers on Stop clicked
    override fun onStopClicked() {
        if (isConnected) {
            mediaControlBinder.stop()
            nowPlayingTextViewModel.setText("")
            controlsFragment.clearBook()
        }
    }

    // ControlsFragment method which triggers on SeekBar changed
    override fun onSeekBarChanged(progress: Int) {
        if (isConnected) {
            mediaControlBinder.seekTo(progress)
        }
    }

    override fun onBackPressed() {
        val bookSelected = bookViewModel.getBook().value != null

        if (bookDetailsContainer == null) { // portrait
            if (bookSelected) {
                if (!mediaControlBinder.isPlaying) {
                    bookViewModel.clearBook()
                }

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
