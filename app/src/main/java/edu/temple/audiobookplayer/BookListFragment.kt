package edu.temple.audiobookplayer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

private const val ARG_PARAM_BOOK_LIST = "bookListParam"

class BookListFragment() : Fragment() {
    private var bookListParam: BookList? = null
    private lateinit var bookViewModel: BookViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            bookListParam = it.getParcelable(ARG_PARAM_BOOK_LIST)
        }

        bookViewModel = ViewModelProvider(requireActivity()).get(BookViewModel::class.java)
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

        layout.findViewById<RecyclerView>(R.id.bookListRecycler).apply {
            adapter = bookListParam?.let {
                BookListAdapter(bookListParam!!, clickEvent)
            }
            layoutManager = LinearLayoutManager(requireContext())
        }

        return layout
    }

    companion object {
        fun newInstance(books: BookList) =
            BookListFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM_BOOK_LIST, books)
                }
            }
    }

    interface BookListFragment {
        fun bookSelected()
    }
}