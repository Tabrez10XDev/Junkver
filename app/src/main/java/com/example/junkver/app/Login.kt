package com.example.junkver.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.junkver.R
import com.google.firebase.auth.FirebaseAuth



class Login : AppCompatActivity() {




    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.FullscreenTheme)

        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        setContentView(R.layout.login)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)



    }





}
