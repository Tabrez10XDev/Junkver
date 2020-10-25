package com.example.junkver.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.junkver.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_create_server.*
import kotlinx.android.synthetic.main.fragment_f_signup.*


class CreateServer : Fragment() {
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
        return inflater.inflate(R.layout.fragment_create_server, container, false)
    }
    lateinit var fireStore : FirebaseFirestore

    lateinit var auth : FirebaseAuth
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        visible = true

        fullscreenContent = view.findViewById(R.id.fullscreen_content)
        fullscreenContentControls = view.findViewById(R.id.fullscreen_content_controls)

        hidebar()
        auth = FirebaseAuth.getInstance()
        fireStore = FirebaseFirestore.getInstance()
        createbutton.setOnClickListener {
            showbar()
            val view = activity?.currentFocus
            view?.let { v ->
                val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(v.windowToken, 0)
            }
            val servername = createtv.text.toString()
            if(servername.isNotEmpty()){
                createServer(servername)
            }
            else{
                hidebar()
                createtv.error = "Invalid"
            }
        }

        creategoback.setOnClickListener {
            findNavController().navigate(R.id.action_createServer_to_server)
        }


    }

    private fun createServer(servername : String){
        var admins = mutableListOf<String>()
        val time = java.sql.Timestamp(System.currentTimeMillis())
        val admin = auth.uid
        admin?.let { admins.add(it) }


        val server = fireStore.collection("servers").document()
        val ref = server.id
        val user = hashMapOf(
            "SID" to servername ,
            "createdAt" to time,
            "Admin" to admins,
            "joinID" to ref

        )
        server.set(user).addOnSuccessListener {
            Toast.makeText(activity,"Created Successfully",Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_createServer_to_existing)
            hidebar()
        }.addOnFailureListener {
            hidebar()
            Toast.makeText(activity,"UnSuccessfull Try again",Toast.LENGTH_SHORT).show()

        }.addOnCanceledListener {
            hidebar()
            Toast.makeText(activity,"Error!",Toast.LENGTH_SHORT).show()

        }
    }



    override fun onResume() {
        super.onResume()
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)


        delayedHide(100)
    }

    private fun showbar(){
        createBar.visibility = View.VISIBLE
        createbutton.isEnabled = false

    }

    private fun hidebar(){
        createBar.visibility = View.INVISIBLE
        createbutton.isEnabled = true
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
        fullscreenContentControls?.visibility = View.GONE
        visible = false

        hideHandler.removeCallbacks(showPart2Runnable)
        hideHandler.postDelayed(hidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    @Suppress("InlinedApi")
    private fun show() {
        fullscreenContent?.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        visible = true

        hideHandler.removeCallbacks(hidePart2Runnable)
        hideHandler.postDelayed(showPart2Runnable, UI_ANIMATION_DELAY.toLong())
        (activity as? AppCompatActivity)?.supportActionBar?.show()
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