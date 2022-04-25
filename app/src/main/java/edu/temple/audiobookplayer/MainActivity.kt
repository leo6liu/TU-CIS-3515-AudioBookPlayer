package edu.temple.audiobookplayer

import android.app.DownloadManager
import android.app.SearchManager
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import edu.temple.audlibplayer.PlayerService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.*
import java.net.URL

const val BOOK_LIST_FILE = "book_list.bin"

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

    // books database
    private lateinit var bookDB: BookDatabase

    // service
    var isConnected = false
    lateinit var mediaControlBinder: PlayerService.MediaControlBinder
    val progressHandler = Handler(Looper.getMainLooper()) {
        if (isConnected) {
            it.obj?.let { obj ->
                // get progress
                val progress = (obj as PlayerService.BookProgress).progress

                // update progress in seekbar
                controlsFragment.updateProgress(progress)

                // update progress in database
                bookViewModel.getPlayingBook().value?.let { playingBook ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        bookDB.bookDao().getById(playingBook.id).let { dbBook ->
                            bookDB.bookDao().updatePosition(dbBook.id, progress)
                        }
                    }
                }
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

        // create book database
        bookDB = Room.databaseBuilder(applicationContext, BookDatabase::class.java, "books").build()

        // trigger search dialog on button click
        val searchButton = findViewById<Button>(R.id.search)
        searchButton.setOnClickListener {
            onSearchRequested()
        }

        // setup containers, fragments
        bookDetailsContainer = findViewById(R.id.containerBookDetails)
        controlsFragment = ControlsFragment()
        bookListFragment = BookListFragment()
        bookDetailsFragment = BookDetailsFragment()

        // initialize viewmodels
        bookViewModel = ViewModelProvider(this).get(BookViewModel::class.java)
        bookListViewModel = ViewModelProvider(this).get(BookListViewModel::class.java)
        nowPlayingTextViewModel = ViewModelProvider(this).get(NowPlayingTextViewModel::class.java)

        // read book list from file
        lifecycleScope.launch(Dispatchers.IO) {
            val file = File(filesDir, BOOK_LIST_FILE)
            if (file.exists()) {
                println("[ STATUS ] reading book list from file storage")
                val fis = FileInputStream(file)
                val ois = ObjectInputStream(fis)
                val readBooks = ois.readObject() as BookList
                ois.close()
                bookListViewModel.setList(readBooks)
            }
        }

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
        if (bookDetailsContainer == null) { // portrait
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.containerBookList, bookDetailsFragment)
                .commit()
        }
    }

    // ControlsFragment method which triggers on Play clicked
    override fun onPlayClicked() {
        if (isConnected) {
            val selectedBook: Book? = bookViewModel.getBook().value
            val playingBook: Book? = bookViewModel.getPlayingBook().value

            // pause playing book if selected and playing book are different
            if (selectedBook != null && playingBook != null && selectedBook != playingBook) {
                onPauseClicked()
            }

            // if a book is selected, set it as the playing book and play it
            if (selectedBook != null) {
                bookViewModel.setPlayingBook(selectedBook)

                // set now playing text
                nowPlayingTextViewModel.setText("Now Playing: ${selectedBook.title}")

                lifecycleScope.launch(Dispatchers.IO) {
                    // get book from database
                    var book: Book? = null
                    println("[ DEBUG ] attempting to get book from database")
                    book = bookDB.bookDao().getById(selectedBook.id)
                    println("[ DEBUG ] book from database: $book")

                    // if it exists in the database, play it from file
                    if (book != null) {
                        val file = File(this@MainActivity.filesDir, "${selectedBook.id}.mp3")
                        mediaControlBinder.play(file, book!!.position)
                    }
                    // else, stream book and download it
                    else {
                        // stream book
                        mediaControlBinder.play(selectedBook.id)

                        // download book
                        downloadBook(selectedBook)
                    }
                }
            }
            // else if a book is playing (could be paused), play it
            else if (playingBook != null) {
                // set now playing text
                nowPlayingTextViewModel.setText("Now Playing: ${playingBook.title}")

                lifecycleScope.launch(Dispatchers.IO) {
                    // get book from database
                    var book: Book? = null
                    println("[ DEBUG ] attempting to get book from database")
                    book = bookDB.bookDao().getById(playingBook.id)
                    println("[ DEBUG ] book from database: $book")

                    // if it exists in the database, play it from file
                    if (book != null) {
                        val file = File(this@MainActivity.filesDir, "${playingBook.id}.mp3")
                        mediaControlBinder.play(file, book!!.position)
                    }
                    // else, stream book and download it
                    else {
                        // stream book
                        mediaControlBinder.play(playingBook.id)

                        // download book
                        downloadBook(playingBook)
                    }
                }
            }
        }
    }

    // ControlsFragment method which triggers on Pause clicked
    override fun onPauseClicked() {
        if (isConnected && mediaControlBinder.isPlaying) {
            mediaControlBinder.pause()
            bookViewModel.getPlayingBook().value?.let { book ->
                nowPlayingTextViewModel.setText("Paused: ${book.title}")
            }
        }
    }

    // ControlsFragment method which triggers on Stop clicked
    override fun onStopClicked() {
        if (isConnected) {
            // stop playing
            mediaControlBinder.stop()

            // clear now playing text
            nowPlayingTextViewModel.setText("")

            // set seekbar to 0
            controlsFragment.clearBook()

            // set database book position to 0
            bookViewModel.getPlayingBook().value?.let { playingBook ->
                lifecycleScope.launch(Dispatchers.IO) {
                    bookDB.bookDao().getById(playingBook.id).let { dbBook ->
                        bookDB.bookDao().updatePosition(dbBook.id, 0)
                    }
                }
            }

            // set playing book to null
            bookViewModel.clearPlayingBook()
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

        // write book list to file storage
        lifecycleScope.launch(Dispatchers.IO) {
            println("[ STATUS ] writing book list to file storage")
            val file = File(filesDir, BOOK_LIST_FILE)
            val fos = FileOutputStream(file)
            val oos = ObjectOutputStream(fos)
            oos.writeObject(books)
            oos.close()
        }

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

    // downloads mp3 file to internal storage and creates a database entry
    private suspend fun downloadBook(book: Book) {
        println("[ STATUS ] downloading book: ${book.id}")

        // download url
        val url = URL("https://kamorris.com/lab/audlib/download.php?id=${book.id}")

        // download mp3 file
        val file = File(this.filesDir, "${book.id}.mp3")
        if (!file.exists()) {
            println("[ STATUS ] downloading mp3 file: url=$url, file=$file")
            withContext(Dispatchers.IO) {
                url.openStream().use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
            println("[ STATUS ] mp3 file downloaded: url=$url, file=$file")

            // add book to database
            bookDB.bookDao().insertAll(book)
        }
    }
}
