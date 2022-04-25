package edu.temple.audiobookplayer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Picasso

class ControlsFragment : Fragment() {
    private lateinit var nowPlaying: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var bookViewModel: BookViewModel
    private lateinit var nowPlayingTextViewModel: NowPlayingTextViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bookViewModel = ViewModelProvider(requireActivity()).get(BookViewModel::class.java)
        nowPlayingTextViewModel = ViewModelProvider(requireActivity()).get(NowPlayingTextViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_controls, container, false)

        layout.findViewById<FloatingActionButton>(R.id.buttonPlay).setOnClickListener { (requireActivity() as ControlsFragment).onPlayClicked() }
        layout.findViewById<FloatingActionButton>(R.id.buttonPause).setOnClickListener { (requireActivity() as ControlsFragment).onPauseClicked() }
        layout.findViewById<FloatingActionButton>(R.id.buttonStop).setOnClickListener { (requireActivity() as ControlsFragment).onStopClicked() }

        // seekbar
        seekBar = layout.findViewById(R.id.seekBar)
        bookViewModel.getPlayingBook().observe(requireActivity()) {
            it?.let { seekBar.max = it.duration }
        }
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    (requireActivity() as ControlsFragment).onSeekBarChanged(progress)
                }
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

        // nowplaying text
        nowPlaying = layout.findViewById(R.id.nowPlaying)
        nowPlayingTextViewModel.getText().observe(requireActivity()) {
            nowPlaying.text = it
        }

        return layout
    }

    fun clearBook() {
        seekBar.progress = 0
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