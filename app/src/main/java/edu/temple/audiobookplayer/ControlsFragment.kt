package edu.temple.audiobookplayer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Picasso

class ControlsFragment : Fragment() {
    private lateinit var nowPlaying: TextView
    private lateinit var seekBar: SeekBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_controls, container, false)

        layout.findViewById<FloatingActionButton>(R.id.buttonPlay).setOnClickListener { (requireActivity() as ControlsFragment).onPlayClicked() }
        layout.findViewById<FloatingActionButton>(R.id.buttonPause).setOnClickListener { (requireActivity() as ControlsFragment).onPauseClicked() }
        layout.findViewById<FloatingActionButton>(R.id.buttonStop).setOnClickListener { (requireActivity() as ControlsFragment).onStopClicked() }
        seekBar = layout.findViewById(R.id.seekBar)
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    (requireActivity() as ControlsFragment).onSeekBarChanged(progress)
                }
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
        nowPlaying = layout.findViewById(R.id.nowPlaying)

        return layout
    }

    fun updateBook(book: Book) {
        nowPlaying.text = book.title
        seekBar.max = book.duration
    }

    fun updateProgress(progress: Int) {
        val seekBar = view?.findViewById<SeekBar>(R.id.seekBar)
        seekBar?.progress = progress
    }

    interface ControlsFragment {
        fun onPlayClicked()
        fun onPauseClicked()
        fun onStopClicked()
        fun onSeekBarChanged(progress: Int)
    }
}