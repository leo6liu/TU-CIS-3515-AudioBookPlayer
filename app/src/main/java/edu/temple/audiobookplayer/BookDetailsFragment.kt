package edu.temple.audiobookplayer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

private const val ARG_PARAM_BOOK = "bookParam"

class BookDetailsFragment() : Fragment() {
    private var bookParam: Book? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            bookParam = it.getParcelable(ARG_PARAM_BOOK)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_book_details, container, false)

        layout.findViewById<TextView>(R.id.detailTitle).apply {
            text = bookParam!!.title
        }

        layout.findViewById<TextView>(R.id.detailAuthor).apply {
            text = bookParam!!.author
        }

        return layout
    }

    companion object {
        fun newInstance(book: Book) =
            BookDetailsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PARAM_BOOK, book)
                }
            }
    }
}