package edu.temple.audiobookplayer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class BookListFragment() : Fragment() {
    private lateinit var bookViewModel: BookViewModel
    private lateinit var bookListViewModel: BookListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bookViewModel = ViewModelProvider(requireActivity()).get(BookViewModel::class.java)
        bookListViewModel = ViewModelProvider(requireActivity()).get(BookListViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_book_list, container, false)

        val clickEvent = { book: Book ->
            bookViewModel.setBook(book)
            (requireActivity() as BookListFragment).bookSelected()
        }

        val bookListAdapter = BookListAdapter(BookList(ArrayList<Book>(0)), clickEvent)

        layout.findViewById<RecyclerView>(R.id.bookListRecycler).apply {
            adapter = bookListAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        bookListViewModel.getList().observe(requireActivity()) {
            bookListAdapter.updateBooks(it)
        }

        return layout
    }

    interface BookListFragment {
        fun bookSelected()
    }
}