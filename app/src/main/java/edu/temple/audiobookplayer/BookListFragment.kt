package edu.temple.audiobookplayer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class BookListFragment() : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_book_list, container, false)

        layout.findViewById<RecyclerView>(R.id.bookListRecycler).apply {
            adapter = BookListAdapter(BookList.generateList()) // TODO: should use BookList in newInstance function
            layoutManager = LinearLayoutManager(this@BookListFragment.context)
        }

        return layout
    }

    companion object {
        fun newInstance(books: BookList): BookListFragment {
            val fragment = BookListFragment()
            return fragment
        }
    }
}