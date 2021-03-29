package com.berkakbas.magictext.controller

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer.OnPreparedListener
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.berkakbas.magictext.R
import com.berkakbas.magictext.model.TextHolder.coordinateMap
import kotlinx.android.synthetic.main.activity_video_play.*

const val MAX_INPUT_LENGTH = 200
const val THREAD_SLEEP = 30L

class VideoPlayActivity : Activity(){
    //Handler for the Video Seek Bar
    private val barHandler: Handler = Handler()

    //Runnable & Handler for adding text to video
    private var textAdder: Runnable= Runnable {  }
    private val textAddHandler: Handler = Handler()
    //Counter to track the frame number
    private var frameCounter =0

    //Tracker for play-pause button's state
    private var buttonLock = "Play"

    //Text input field to add to video
    private var inputText=""

    var inputTexts= ArrayList<TextView>()
    //Index for the [input text,coordinates] hashmap
    var hmindex=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_play)

        //Create textviews for every related input texts
        coordinateMap.forEach{
            var rowTextView = TextView(this)
            rowTextView.text= it.key
            rowTextView.setTextColor(getResources().getColor(R.color.white))
            rowTextView.id= getResources().getIdentifier(it.key, "textView", getPackageName())
            var params= firstScreenLayout.layoutParams
            rowTextView.setLayoutParams(params)

            inputTexts.add(rowTextView)
            firstScreenLayout.addView(rowTextView)
        }

        //Get video file for videoView
        videoView.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.video)
        videoView.seekTo(1)
        //Return to start when video finishes
        videoView.setOnCompletionListener { mp ->
            textAddHandler.removeCallbacks(textAdder)
            mp.seekTo(1)
            mp.pause()
            buttonLock= "Play"
            playButton.text= buttonLock
            addTextButton.visibility = View.VISIBLE
            frameCounter= 0
            hmindex=0
        }
        //Handle Video Seek Bar
        videoView.setOnPreparedListener {
            seekBar.progress = 0
            seekBar.max = videoView.getDuration()
            barHandler.post(updateVideoTime)
        }

        playButton.setOnClickListener {
            playButton.isEnabled= false
            //Play state for button tracker
            if(buttonLock.equals("Play")){
                //Go over the [input text,coordinates] hashmap in every 30 miliseconds
                textAdder = object : Runnable {
                    override fun run() {
                        seekBar.setProgress(videoView.getCurrentPosition())

                        if(!coordinateMap.isEmpty()){
                            //Show the text view in the right position for every input text for every frame
                            coordinateMap.forEach{
                                if(it.value.size>frameCounter){
                                    inputTexts.get(hmindex).x= it.value.get(frameCounter).x
                                    inputTexts.get(hmindex).y= it.value.get(frameCounter).y
                                    inputTexts.get(hmindex).visibility= View.VISIBLE

                                }else{
                                    inputTexts.get(hmindex).visibility= View.INVISIBLE
                                }
                                hmindex++
                            }
                            frameCounter++
                            hmindex=0
                            textAddHandler.postDelayed(this, THREAD_SLEEP)
                        }

                    }
                }
                textAddHandler.post(textAdder)
                buttonLock= "Pause"
                playButton.text= buttonLock
                videoView.start()
                playButton.isEnabled= true
                addTextButton.visibility = View.INVISIBLE
            }
            //Pause state for button tracker
            else if(buttonLock.equals("Pause")){
                textAddHandler.removeCallbacks(textAdder)
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
            val inputPlainText = EditText(this)
            builder.setView(inputPlainText)

            builder.setPositiveButton("OK") {
                    //Jump to video editing activity if the input text is appropriate
                    dialog, which ->
                inputText = inputPlainText.text.toString()
                if(inputText.length > MAX_INPUT_LENGTH){
                    Toast.makeText(this@VideoPlayActivity,"Your input length is too long.",Toast.LENGTH_LONG).show()
                }else if(inputText.length == 0){
                    Toast.makeText(this@VideoPlayActivity,"Enter a text input.",Toast.LENGTH_LONG).show()
                }
                else{
                    intent = Intent(this, VideoEditActivity::class.java)
                    intent.putExtra("input", inputText)
                    startActivity(intent)
                }
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
            barHandler.postDelayed(this, THREAD_SLEEP)
        }
    }

}


