package com.surendramaran.yolov8tflite

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity



class HelpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        // ボタン要素（オブジェクト）を取得
        val buttonToMainActivity = findViewById<ImageButton>(R.id.button_to_main)

        // ボタンタップ時のイベントリスナー
        buttonToMainActivity.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
