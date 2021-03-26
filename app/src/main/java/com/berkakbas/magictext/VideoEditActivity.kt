package com.berkakbas.magictext

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_video_edit.*

class VideoEditActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_edit)

        //save current video edit
        saveButton.setOnClickListener {
            intent = Intent(this, VideoPlayActivity::class.java);
            startActivity(intent)
        }

    }

}