package edu.temple.audiobookplayer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class BookListAdapter (
    private val books: BookList,
) : RecyclerView.Adapter<BookListAdapter.ViewHolder>() {
    /**
     * ViewHolder for each book in list
     */
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        lateinit var book: Book
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context)
            .inflate(R.layout.book_list_recycler_element, parent, false)
        return ViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.book = books[position]
    }

    override fun getItemCount(): Int {
        return books.size
    }

}