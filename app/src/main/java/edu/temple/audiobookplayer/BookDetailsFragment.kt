package edu.temple.audiobookplayer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.squareup.picasso.Picasso

class BookDetailsFragment() : Fragment() {
    lateinit var bookViewModel: BookViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bookViewModel = ViewModelProvider(requireActivity()).get(BookViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_book_details, container, false)

        val titleView = layout.findViewById<TextView>(R.id.detailTitle)
        val authorView = layout.findViewById<TextView>(R.id.detailAuthor)
        val imageView = layout.findViewById<ImageView>(R.id.detailImage)

        bookViewModel.getBook().observe(requireActivity()) {
            if (it != null) {
                titleView.apply { text = it.title }
                authorView.apply { text = it.author }
                Picasso.get().load(it.coverURL).into(imageView)
            } else {
                titleView.apply { text = "" }
                authorView.apply { text = "" }
                imageView.setImageResource(android.R.color.transparent)
            }
        }

        return layout
    }
}