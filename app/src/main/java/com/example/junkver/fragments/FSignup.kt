package com.example.junkver.fragments

import android.app.Activity
import android.app.Person
import android.content.Context
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
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.junkver.R
import com.example.junkver.app.Dashboard
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_f_signup.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class FSignup : Fragment() {
    private val hideHandler = Handler()

    @Suppress("InlinedApi")
    private val hidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
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
        // Delayed display of UI elements
        fullscreenContentControls?.visibility = View.VISIBLE
    }
    private var visible: Boolean = false
    private val hideRunnable = Runnable { hide() }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */


    private var fullscreenContent: View? = null
    private var fullscreenContentControls: View? = null
    lateinit var auth : FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_f_signup, container, false)
    }


    var selecturi : Uri?= null
    lateinit var storageRef : FirebaseStorage

    lateinit var fireStore : FirebaseFirestore

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        visible = true

        fullscreenContent = view.findViewById(R.id.fullscreen_content)
        fullscreenContentControls = view.findViewById(R.id.fullscreen_content_controls)
        auth = FirebaseAuth.getInstance()
        fireStore = FirebaseFirestore.getInstance()



        storageRef = FirebaseStorage.getInstance()
        hidebar()
        bphoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "image/*"
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            startActivityForResult(Intent.createChooser(intent, "Select image"),1);
        }

        bregister.setOnClickListener {
            val view = activity?.currentFocus
            view?.let { v ->
                val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(v.windowToken, 0)
            }
            registerUser()

        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if(requestCode == 1 && resultCode == Activity.RESULT_OK && data != null ){
            selecturi = data.data
            bphoto.alpha = 0f
            val bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, selecturi)
            circlesign.setImageBitmap(bitmap)

        }
    }


    private fun savePerson(username : String) = CoroutineScope(Dispatchers.IO).launch {
        try {

            Log.d("taby","inside")
            val time = java.sql.Timestamp(System.currentTimeMillis())
            val user = hashMapOf(
                "UID" to auth.uid,
                "username" to username,
                "photoUri" to ""

            )
            auth.uid?.let {
                var personCollection = fireStore.collection("persons").document(auth.uid!!)
                    personCollection.set(user as Map<String, Any>).addOnFailureListener {
                }.addOnSuccessListener {

                        uploadPhoto(personCollection)
                }


            }


        }
        catch (e : Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(activity,e.message+" Update your profile again",Toast.LENGTH_LONG).show()
                Log.d("taby",e.message  )

            }
        }
    }
    private fun uploadPhoto(personCollection : DocumentReference){
        val imageRef = storageRef.reference.child(auth.uid!!)
        selecturi?.let {
              imageRef.putFile(it).addOnSuccessListener {

                  it.storage.downloadUrl.addOnSuccessListener {  uri->
                  personCollection.update("photoUri",uri.toString())

          }
              }
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
                        Toast.makeText(activity,"Account created",Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener(){
                        Toast.makeText(activity,it.message,Toast.LENGTH_SHORT).show()
                        hidebar()
                    }


                }
                catch (e : Exception){
                    withContext(Dispatchers.Main){
                        hidebar()
                        Toast.makeText(activity,e.message,Toast.LENGTH_SHORT).show()
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

                            savePerson(username)
                            hidebar()
                            Toast.makeText(activity, "Updated username", Toast.LENGTH_SHORT).show()
                            checkLoggedInState()

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

    private fun checkLoggedInState(){
        if(auth.currentUser == null){
        }
        else{
            Toast.makeText(activity,"Logged in as " + auth.currentUser?.displayName,Toast.LENGTH_SHORT).show()
val intent = Intent(activity, Dashboard::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)

            startActivity(intent)
            activity?.finish()
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
//        bphoto.alpha = 1f


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