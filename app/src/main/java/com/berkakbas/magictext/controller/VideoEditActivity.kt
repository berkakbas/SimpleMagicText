package com.berkakbas.magictext.controller

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import com.berkakbas.magictext.R
import com.berkakbas.magictext.model.Coordinate
import com.berkakbas.magictext.model.TextHolder
import kotlinx.android.synthetic.main.activity_video_edit.*


class VideoEditActivity : Activity() {

    var input: String = ""
    //Runnable & Handler for the input text demonstration
    private var textEditor: Runnable= Runnable {  }
    private val editorHandler: Handler = Handler()

    var statusBarHeight = 0

    //Tracker for the coordinates of the textView
    var currentTextCoordinates= ArrayList<Coordinate>()

    //Handler for the Video Seek Bar
    private val editBarHandler: Handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_edit)

        //Input text from dialog
        input = intent.getStringExtra("input")!!
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
        //Get height of status bar to calculate the y coordinate right:
        var resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android")
        statusBarHeight = getResources().getDimensionPixelSize(resourceId)

        //Handle Video Seek Bar
        editVideoView.setOnPreparedListener {
            editSeekBar.progress = 0
            editSeekBar.max = editVideoView.getDuration()
            editBarHandler.post(updateEditVideoTime)
        }

        //Save current video edit to TextHolder List
        saveButton.setOnClickListener {
            TextHolder.coordinateMap.put(input,currentTextCoordinates)
            TextHolder.allTextCoordinates.add(currentTextCoordinates)
            intent = Intent(this, VideoPlayActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        //Listener for putting the input text into Video View
        editVideoView.setOnTouchListener { v, event ->
            var currentCoordinate: Coordinate

            when(event.action){
                MotionEvent.ACTION_DOWN
                -> {//Play the video when touched and take the coordinates of the first motion
                    editVideoView.start()
                    videoText.visibility = View.VISIBLE
                    //Start the Runnable for the whole motion
                    textEditor = object : Runnable {
                        override fun run() {
                            //Calculate the coordinates of input text and keep them in order
                            videoText.x = event.x - videoText.width / 2
                            videoText.y = event.y - videoText.height / 2 - statusBarHeight
                            currentCoordinate = Coordinate(videoText.x, videoText.y)
                            currentTextCoordinates.add(currentCoordinate)
                            editorHandler.postDelayed(this, THREAD_SLEEP)
                        }
                    }
                    editorHandler.post(textEditor)
                }
                MotionEvent.ACTION_UP -> {//Pause the video when finger lifted and take the coordinates of the last motion
                    editorHandler.removeCallbacks(textEditor)

                    editVideoView.pause()
                    //Calculate the coordinates of last position of input text
                    videoText.x = event.x + editVideoView.x - videoText.width / 2
                    videoText.y = event.y + editVideoView.y - videoText.height / 2
                    currentCoordinate = Coordinate(videoText.x, videoText.y)
                    currentTextCoordinates.add(currentCoordinate)
                }
            }
            true
        }

    }

    //Runnable object for Video Seek Bar
    val updateEditVideoTime = object : Runnable {
        override fun run() {
            editSeekBar.setProgress(editVideoView.getCurrentPosition())
            editBarHandler.postDelayed(this, THREAD_SLEEP)
        }
    }

}