package com.robotemi.bridge

import android.app.ActivityOptions
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url = intent.getStringExtra("url")
        if (url.isNullOrBlank().not()) {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(url)
            ).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            try {
                // start chrome
                intent.setPackage("com.android.chrome")
                startActivity(
                    intent,
                    ActivityOptions.makeCustomAnimation(
                        applicationContext,
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                    ).toBundle()
                )
            } catch (e: ActivityNotFoundException) {
                intent.setPackage(null)
                startActivity(
                    intent,
                    ActivityOptions.makeCustomAnimation(
                        applicationContext,
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                    ).toBundle()
                )
            }
        }
        finish()
        overridePendingTransition(0, 0)
    }
}