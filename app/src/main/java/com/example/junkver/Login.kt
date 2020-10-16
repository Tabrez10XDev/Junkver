package com.example.junkver

import androidx.appcompat.app.AppCompatActivity
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.login.*
import kotlinx.android.synthetic.main.login.tvpass
import kotlinx.android.synthetic.main.login.tvsign
import kotlinx.android.synthetic.main.signup.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class Login : AppCompatActivity() {
    private lateinit var fullscreenContent: TextView
    private lateinit var fullscreenContentControls: LinearLayout
    private val hideHandler = Handler()

    @SuppressLint("InlinedApi")
    private val hidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        fullscreenContent.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }
    private val showPart2Runnable = Runnable {
        // Delayed display of UI elements
        supportActionBar?.show()
        fullscreenContentControls.visibility = View.VISIBLE
    }
    private var isFullscreen: Boolean = false

    private var hideRunnable = Runnable { hide() }

    override fun onResume() {
        super.onResume()
        delayedHide(100)
    }

    lateinit var auth : FirebaseAuth

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        setContentView(R.layout.login)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        progresslog.visibility = View.INVISIBLE

        auth.signOut()
        isFullscreen = true

        // Set up the user interaction to manually show or hide the system UI.
        fullscreenContent = findViewById(R.id.fullscreen_content)
//        fullscreenContent.setOnClickListener { toggle() }

        fullscreenContentControls = findViewById(R.id.fullscreen_content_controls)

        blogin.setOnClickListener {
            val view = this.currentFocus
            view?.let { v ->
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(v.windowToken, 0)
            }
            loginUser()
        }

        bsignup.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

        }
        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
//        findViewById<Button>(R.id.dummy_button).setOnTouchListener(delayHideTouchListener)
    }

    override fun onStart() {
        super.onStart()
//        checkLoggedInState()
    }

    private fun loginUser() {
        showbar()
        val email = tvsign.text.toString()
        val password = tvpass.text.toString()
        if( email.isNotEmpty() && password.isNotEmpty()){
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    auth.signInWithEmailAndPassword(email,password).addOnSuccessListener {
                        hidebar()
                        checkLoggedInState()
                    }.addOnFailureListener {
                            hidebar()
                            Toast.makeText(this@Login, "Invalid Credentials", Toast.LENGTH_SHORT).show()

                    }.addOnCanceledListener {
                        hidebar()
                        Toast.makeText(this@Login, "Error!", Toast.LENGTH_SHORT).show()

                    }

                }
                catch (e : Exception){
                    withContext(Dispatchers.Main){
                        hidebar()
                        Toast.makeText(this@Login,e.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun checkLoggedInState(){
        if(auth.currentUser != null){
            Toast.makeText(this@Login,"Logged in as " + auth.currentUser?.displayName, Toast.LENGTH_SHORT).show()
            startActivity(Intent(this,Dashboard::class.java))

        }
    }

    private fun hidebar(){
        progresslog.visibility = View.INVISIBLE
        blogin.isEnabled = true
        bsignup.isEnabled = true
    }

    private fun showbar(){
        progresslog.visibility = View.VISIBLE
        blogin.isEnabled = false
        bsignup.isEnabled = false
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

//    private fun toggle() {
//        if (isFullscreen) {
//            hide()
//        } else {
//            show()
//        }
//    }

    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
        fullscreenContentControls.visibility = View.GONE
        isFullscreen = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        hideHandler.removeCallbacks(showPart2Runnable)
        hideHandler.postDelayed(hidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

//    private fun show() {
//        // Show the system bar
//        fullscreenContent.systemUiVisibility =
//            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
//                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//        isFullscreen = true
//
//        // Schedule a runnable to display UI elements after a delay
//        hideHandler.removeCallbacks(hidePart2Runnable)
//        hideHandler.postDelayed(showPart2Runnable, UI_ANIMATION_DELAY.toLong())
//    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, delayMillis.toLong())
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300
    }
}