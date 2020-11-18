package com.example.junkver.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.marginBottom
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.junkver.R
import com.example.junkver.adapter.InsideAdap
import com.example.junkver.app.Dashboard
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.dashboard_bar.*
import kotlinx.android.synthetic.main.fragment_inside.*


/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class InsideFragment : Fragment() {
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
        return inflater.inflate(R.layout.fragment_inside, container, false)
    }
    lateinit var adapter : InsideAdap
    lateinit var joinID : String
    lateinit var fireStore : FirebaseFirestore
    lateinit var auth : FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        visible = true
        fullscreenContent = view.findViewById(R.id.fullscreen_content)
        fullscreenContentControls = view.findViewById(R.id.fullscreen_content_controls)

        val sid = arguments?.getString("SID")
        joinID = arguments?.getString("joinID").toString()
        (activity as Dashboard).toolbar.title = sid

        fireStore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        setUpRV()



        sendText.setOnClickListener {
            (activity as Dashboard).clicked = true
        }

        sendbutton.setOnClickListener {

            val txt = sendText.text
            if(txt.isNotEmpty()){
                sendMessage()
            }

        }
        (activity as Dashboard).toolbar?.menu?.findItem(R.id.shareLink)?.setVisible(true)

        (activity as Dashboard).toolbar.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.shareLink-> {
                    var clipboard = (activity as Dashboard).getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    var clip = ClipData.newPlainText("label",joinID)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(activity,"Server ID copied in your clipboard, paste it in JoinID",Toast.LENGTH_LONG).show()
                    return@setOnMenuItemClickListener true
                }
                else->{
                    return@setOnMenuItemClickListener true
                }

            }
        }
        chatRV.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if(bottom < oldBottom) {
                chatRV.scrollToPosition(adapter.itemCount - 1)
            }

        }


        subscribeToChannel()



    }




    private fun subscribeToChannel(){

        val channel = fireStore.collection("servers").document(joinID).collection("messages")

                channel.orderBy("createdAt",Query.Direction.ASCENDING).addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                firebaseFirestoreException?.let {
                    return@addSnapshotListener
                }
                querySnapshot?.let {documents->
                    val sb : MutableList<Map<String,Any>> = arrayListOf()

                    for(document in documents) {
                        sb.add(document.data)

                    }
                    adapter.differ2.submitList(sb)

                }
            }



    }

    private fun sendMessage(){
        val time = java.sql.Timestamp(System.currentTimeMillis())

        val txt = sendText.text.toString()
        val message = hashMapOf(
            "text" to txt,
            "username" to auth.currentUser?.displayName,
            "createdAt" to time,
            "UID" to auth.uid
        )
        sendText.setText("")

        fireStore.collection("servers").document(joinID).collection("messages").document().set(message)
            .addOnFailureListener {
            //TOODO
             }
            .addOnSuccessListener {
            fireStore.collection("servers").document(joinID).update("createdAt",time)
                fireStore.collection("servers").document(joinID).update("Last",txt)

            }
    }

    private fun setUpRV() {
        adapter = InsideAdap()
        val manager = LinearLayoutManager(activity)
        manager.stackFromEnd = true
        chatRV.layoutManager = manager
      chatRV.adapter = adapter


  }






    override fun onResume() {
        super.onResume()
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        Log.d("poda","chillman")

//        delayedHide(100)
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




        private const val UI_ANIMATION_DELAY = 50
    }
}