package com.example.junkver.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.junkver.R
import com.example.junkver.app.Dashboard
import com.example.junkver.app.Login
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class Profile : Fragment() {
    private val hideHandler = Handler()
    @Suppress("InlinedApi")
    private val hidePart2Runnable = Runnable {

        val flags =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        activity?.window?.decorView?.systemUiVisibility = flags
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
    }
    private val showPart2Runnable = Runnable {
        fullscreenContentControls?.visibility = View.VISIBLE
    }
    private var visible: Boolean = false
    private val hideRunnable = Runnable { hide() }



    private var fullscreenContent: View? = null
    private var fullscreenContentControls: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    var selecturi : Uri ?= null
    lateinit var auth : FirebaseAuth
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        visible = true

        fullscreenContent = view.findViewById(R.id.fullscreen_content)
        fullscreenContentControls = view.findViewById(R.id.fullscreen_content_controls)
        auth = FirebaseAuth.getInstance()
        selecturi = auth.currentUser!!.photoUrl
        if(selecturi!= null){
            profbutton.alpha = 0f
        }
        hidebar()
        Glide.with(this).load(selecturi).into(profphoto)
        val email = auth.currentUser?.email
        profmail.setText(email)
        profuser.setText(auth.currentUser?.displayName)

        profbutton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Select image"),2);
        }

        profupdate.setOnClickListener {
            showbar()
            updateProfile()
        }

        proflogout.setOnClickListener{
            auth.signOut()
            startActivity(Intent(activity,Login::class.java))
            activity?.finish()
        }




    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if(requestCode == 2 && resultCode == Activity.RESULT_OK && data != null ){
            selecturi = data.data
            profbutton.alpha = 0f
            Glide.with(this).load(selecturi).into(profphoto)


        }
    }

    private fun showbar(){
        profbar.visibility = View.VISIBLE
        profupdate.isEnabled = false

    }

    private fun hidebar(){
      profbar.visibility = View.INVISIBLE
      profupdate.isEnabled = true
    }
   lateinit var profileUpdate : UserProfileChangeRequest

    private fun updateProfile() {
        val username = profuser.text.toString()

        if (username.isNotEmpty()){
            auth.currentUser?.let { user ->
                if(selecturi != null) {
                    profileUpdate = UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .setPhotoUri(selecturi)
                        .build()
                }
                else{
                    profileUpdate = UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build()
                }
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        user.updateProfile(profileUpdate).addOnSuccessListener {
                            hidebar()

                            val navView: NavigationView ?= activity?.findViewById(R.id.nav_view)

                            var swipe = navView?.getHeaderView(0)
                            var swipename = swipe?.findViewById<TextView>(R.id.swipename)
                            swipename?.text = username
                            if(selecturi!= null) {
                                var swipephoto =
                                    swipe!!.findViewById<CircleImageView>(R.id.swipephoto)

                                Glide.with(this@Profile).load(selecturi).into(swipephoto)

                                Toast.makeText(activity, "Updated username", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }.addOnFailureListener {
                            hidebar()
                            Toast.makeText(activity, it.message, Toast.LENGTH_SHORT).show()

                        }.addOnCanceledListener {
                            hidebar()
                            Toast.makeText(activity,"Please Login manually and update your username", Toast.LENGTH_SHORT).show()
                        }


                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            hidebar()
                            Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
        else{
            hidebar()
            Toast.makeText(activity,"Please Login manually and update your username", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        if(selecturi== null){
            profbutton.alpha = 1f
        }

        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        delayedHide(100)

    }

    override fun onPause() {
        super.onPause()

        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        activity?.window?.decorView?.systemUiVisibility = 0
        show()
    }

    override fun onDestroy() {
        super.onDestroy()
        fullscreenContent = null
        fullscreenContentControls = null
    }



    private fun hide() {
        // Hide UI first
        fullscreenContentControls?.visibility = View.GONE
        visible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        hideHandler.removeCallbacks(showPart2Runnable)
        hideHandler.postDelayed(hidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    @Suppress("InlinedApi")
    private fun show() {
        // Show the system bar
        fullscreenContent?.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        visible = true

        // Schedule a runnable to display UI elements after a delay
        hideHandler.removeCallbacks(hidePart2Runnable)
        hideHandler.postDelayed(showPart2Runnable, UI_ANIMATION_DELAY.toLong())
        (activity as? AppCompatActivity)?.supportActionBar?.show()
    }

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