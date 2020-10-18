package com.example.junkver

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.android.synthetic.main.login.*
import kotlinx.android.synthetic.main.login.tvpass
import kotlinx.android.synthetic.main.login.tvsign
import kotlinx.android.synthetic.main.signup.*
import kotlinx.coroutines.*
import java.lang.Exception
import java.lang.Runnable

class SignUp: AppCompatActivity() {
    private lateinit var fullscreenContent: TextView
    private lateinit var fullscreenContentControls: LinearLayout
    private val hideHandler = Handler()

    @SuppressLint("InlinedApi")
    private val hidePart2Runnable = Runnable {

        fullscreenContent.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }
    private val showPart2Runnable = Runnable {
        supportActionBar?.show()
        fullscreenContentControls.visibility = View.VISIBLE
    }
    private var isFullscreen: Boolean = false
    private val hideRunnable = Runnable { hide() }


    lateinit var auth : FirebaseAuth

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.signup)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        isFullscreen = true

        fullscreenContent = findViewById(R.id.fullscreen_content)

        fullscreenContentControls = findViewById(R.id.fullscreen_content_controls)
        progresssign.visibility = View.INVISIBLE
        bphoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)

        }

        auth = FirebaseAuth.getInstance()
        bregister.setOnClickListener {
            val view = this.currentFocus
            view?.let { v ->
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(v.windowToken, 0)
            }
            registerUser()
        }

    }

    var selecturi : Uri?= null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 0 && resultCode ==Activity.RESULT_OK && data != null ){
            selecturi = data.data
            bphoto.alpha = 0f

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selecturi)
            circlesign.setImageBitmap(bitmap)
        }
    }

    private fun showbar(){
        progresssign.visibility = View.VISIBLE
        bregister.isEnabled = false

    }

    private fun hidebar(){
        progresssign.visibility = View.INVISIBLE
        bregister.isEnabled = true
    }
    private fun registerUser() {
        showbar()
        val email = tvsign.text.toString()
        val password = tvpass.text.toString()
        if( email.isNotEmpty() && password.isNotEmpty()){
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    auth.createUserWithEmailAndPassword(email,password).addOnSuccessListener {
                        updateProfile()
                        Toast.makeText(this@SignUp,"Account created",Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener(){
                        Toast.makeText(this@SignUp,it.message,Toast.LENGTH_SHORT).show()
                        hidebar()
                    }


                }
                catch (e : Exception){
                    withContext(Dispatchers.Main){
                        hidebar()
                        Toast.makeText(this@SignUp,e.message,Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


    private fun updateProfile() {
        val username = tvuserreg.text.toString()

        if (username.isNotEmpty() && selecturi != null ){
            auth.currentUser?.let { user ->
                val profileUpdate = UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .setPhotoUri(selecturi)
                    .build()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        user.updateProfile(profileUpdate).addOnSuccessListener {
                            hidebar()
//                            Toast.makeText(this@SignUp, "Updated username", Toast.LENGTH_SHORT).show()
                            checkLoggedInState()

                        }.addOnFailureListener {
                            hidebar()
                            Toast.makeText(this@SignUp, it.message, Toast.LENGTH_SHORT).show()

                        }


                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@SignUp, e.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
    }
    }

    private fun checkLoggedInState(){
        if(auth.currentUser == null){
        }
        else{
            Toast.makeText(this@SignUp,"Logged in as " + auth.currentUser?.displayName,Toast.LENGTH_SHORT).show()
val intent = Intent(this,Dashboard::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)

            startActivity(intent)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)


        delayedHide(100)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
    }


    private fun hide() {
        supportActionBar?.hide()
        fullscreenContentControls.visibility = View.GONE
        isFullscreen = false

        hideHandler.removeCallbacks(showPart2Runnable)
        hideHandler.postDelayed(hidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }


    private fun delayedHide(delayMillis: Int) {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, delayMillis.toLong())
    }

    companion object {

        private const val AUTO_HIDE = true


        private const val AUTO_HIDE_DELAY_MILLIS = 3000


        private const val UI_ANIMATION_DELAY = 300
    }
}