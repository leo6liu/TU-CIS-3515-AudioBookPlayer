package edu.temple.audiobookplayer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class BookListFragment() : Fragment() {
    private var bookListParam: BookList? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            bookListParam = it.getParcelable(ARG_PARAM_BOOK_LIST)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_book_list, container, false)

        layout.findViewById<RecyclerView>(R.id.bookListRecycler).apply {
            adapter = bookListParam?.let { BookListAdapter(bookListParam!!) }
            layoutManager = LinearLayoutManager(requireContext())
        }

        return layout
    }

    companion object {
        private const val ARG_PARAM_BOOK_LIST = "bookListParam"

        fun newInstance(books: BookList): BookListFragment =
            BookListFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM_BOOK_LIST, books)
                }
            }
    }
}