package com.berkakbas.magictext

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnPreparedListener
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_video_play.*


class VideoPlayActivity : Activity(){

    private val barHandler: Handler = Handler()

    //Tracker for play-pause button's state
    private var buttonLock = "Play"

    //Text input field to add to video
    private var inputText=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_play)

        //Get video file for videoView
        videoView.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.video)
        //Return to start when video finishes
        videoView.setOnCompletionListener { mp ->
            mp.seekTo(1)
            mp.pause()
            buttonLock= "Play"
            playButton.text= buttonLock
            addTextButton.visibility = View.VISIBLE
        }
        //Handle Video Seek Bar
        videoView.setOnPreparedListener(OnPreparedListener {
            seekBar.progress = 0
            seekBar.max = videoView.getDuration()
            barHandler.postDelayed(updateVideoTime, 100)
        })

        playButton.setOnClickListener {
            playButton.isEnabled= false
            //Play state for button tracker
            if(buttonLock.equals("Play")){
                buttonLock= "Pause"
                playButton.text= buttonLock
                videoView.start()
                playButton.isEnabled= true
                addTextButton.visibility = View.INVISIBLE
            }
            //Pause state for button tracker
            else if(buttonLock.equals("Pause")){
                buttonLock= "Play"
                playButton.text= buttonLock
                videoView.pause()
                playButton.isEnabled= true
                addTextButton.visibility = View.VISIBLE
            }
        }

        addTextButton.setOnClickListener {
            //Set fields of text input dialog
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Text Input")
            val input = EditText(this)
            builder.setView(input)

            builder.setPositiveButton("OK") {
                    //Jump to video editing activity
                    dialog, which ->
                    inputText = input.text.toString()
                    intent = Intent(this, VideoEditActivity::class.java)
                    intent.putExtra("input", inputText)
                    startActivity(intent)
            }

            builder.setNegativeButton("Cancel") {
                    //Stay in the video play activity
                    dialog, which -> dialog.cancel()
            }
            builder.show()
        }

    }
    //Video stop-play logic for onStop() & onPause() methods
    override fun onStop() {
        super.onStop()
        videoView.pause()
    }
    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            videoView.pause()
        }
    }

    //Runnable object for Video Seek Bar
    val updateVideoTime = object : Runnable {
        override fun run() {
            seekBar.setProgress(videoView.getCurrentPosition())
            barHandler.postDelayed(this, 30)
        }
    }
}


