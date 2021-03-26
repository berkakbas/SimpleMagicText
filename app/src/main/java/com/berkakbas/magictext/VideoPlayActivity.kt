package com.berkakbas.magictext

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_video_play.*


class VideoPlayActivity : AppCompatActivity() {
    //Tracker for play-pause button's state
    private var buttonLock = "play"

    //Text input field to add to video
    private var inputText="";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_play)

        //get video file for videoView
        videoView.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.video)

        playButton.setOnClickListener {
            playButton.isEnabled= false
            //Play state for button tracker
            if(buttonLock.equals("play")){
                buttonLock= "pause"
                playButton.text= buttonLock
                videoView.start()
                playButton.isEnabled= true

            }
            //Pause state for button tracker
            else if(buttonLock.equals("pause")){
                buttonLock= "play"
                playButton.text= buttonLock
                videoView.pause();
                playButton.isEnabled= true
            }
        }

        addTextButton.setOnClickListener {
            //Set fields of text input dialog
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Text Input")
            val input = EditText(this)
            builder.setView(input)

            builder.setPositiveButton("OK",
                {
                        //Jump to video editing activity
                        dialog, which -> inputText = input.text.toString()
                        intent = Intent(this, VideoEditActivity::class.java);
                        intent.putExtra("input",inputText)
                        startActivity(intent)

                })

            builder.setNegativeButton("Cancel",
                {
                        //Stay in the video play activity
                        dialog, which -> dialog.cancel()

                })

            builder.show()
        }
    }

    //Video stop-play logic for onStop() & onPause() methods
    override fun onStop() {
        super.onStop()
        videoView.pause();
    }
    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            videoView.pause();
        }
    }

}