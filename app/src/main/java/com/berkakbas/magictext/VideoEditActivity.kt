package com.berkakbas.magictext

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import kotlinx.android.synthetic.main.activity_video_edit.*

const val THREAD_SLEEP = 30L

class VideoEditActivity : Activity() {
    //Tracker for the coordinates of the textView
    var currentTextCoordinates= ArrayList<Coordinate>()

    //Handler for the Video Seek Bar
    private val editBarHandler: Handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_edit)

        //Input text from dialog
        var input: String? = intent.getStringExtra("input")
        videoText.visibility = View.INVISIBLE
        videoText.text= input

        //get video file for videoView
        editVideoView.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.video)
        //Return to start when video finishes
        editVideoView.setOnCompletionListener { mp ->
            mp.seekTo(1)
            mp.pause()
            videoText.visibility= View.INVISIBLE
        }
        //Handle Video Seek Bar
        editVideoView.setOnPreparedListener(MediaPlayer.OnPreparedListener {
            editSeekBar.progress = 0
            editSeekBar.max = editVideoView.getDuration()
            editBarHandler.postDelayed(updateEditVideoTime, 30)
        })

        //Save current video edit to TextHolder List
        saveButton.setOnClickListener {
            TextHolder.allTextCoordinates.add(currentTextCoordinates)
            intent = Intent(this, VideoPlayActivity::class.java)
            startActivity(intent)
            this.finish()
        }

        //Listener for putting the input text into Video View
        editVideoView.setOnTouchListener { v, event ->
            var currentCoordinate: Coordinate

            when(event.action){
                MotionEvent.ACTION_DOWN
                -> {//Play the video when touched and take the coordinates of the first motion
                    editVideoView.start()
                    videoText.visibility = View.VISIBLE
                    currentCoordinate = Coordinate(videoText.x, videoText.y)
                    currentTextCoordinates.add(currentCoordinate)
                    videoText.x = event.x + editVideoView.x - videoText.width / 2
                    videoText.y = event.y + editVideoView.y - videoText.height / 2
                    Thread.sleep(THREAD_SLEEP)
                }
                MotionEvent.ACTION_UP -> {//Pause the video when finger lifted and take the coordinates of the last motion
                    editVideoView.pause()
                    currentCoordinate = Coordinate(videoText.x, videoText.y)
                    currentTextCoordinates.add(currentCoordinate)
                    videoText.x = event.x + editVideoView.x - videoText.width / 2
                    videoText.y = event.y + editVideoView.y - videoText.height / 2
                    Thread.sleep(THREAD_SLEEP)
                }
                MotionEvent.ACTION_MOVE -> {//Take the coordinates while dragging
                    videoText.x = event.x + editVideoView.x - videoText.width / 2
                    videoText.y = event.y + editVideoView.y - videoText.height / 2
                    Thread.sleep(THREAD_SLEEP)
                }
            }
            true
        }

    }
    //Runnable object for Video Seek Bar
    val updateEditVideoTime = object : Runnable {
        override fun run() {
            editSeekBar.setProgress(editVideoView.getCurrentPosition())
            editBarHandler.postDelayed(this, 30)
        }
    }

}
//Data class to hold coordinates of the motion
data class Coordinate(
    val x: Float,
    val y: Float
)