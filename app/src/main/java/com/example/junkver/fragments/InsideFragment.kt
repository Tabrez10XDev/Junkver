package com.example.junkver.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.junkver.R
import com.example.junkver.adapter.InsideAdap
import com.example.junkver.app.Dashboard
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.chat_to.view.*
import kotlinx.android.synthetic.main.dashboard_bar.*
import kotlinx.android.synthetic.main.fragment_inside.*
import kotlinx.android.synthetic.main.fragment_profile.*


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
Log.d("poda","chill")
        visible = true
        fullscreenContent = view.findViewById(R.id.fullscreen_content)
        fullscreenContentControls = view.findViewById(R.id.fullscreen_content_controls)

        val sid = arguments?.getString("SID")
        joinID = arguments?.getString("joinID").toString()
        (activity as Dashboard).toolbar.title = sid
        fireStore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        setUpRV()


        sendbutton.setOnClickListener {
            sendMessage()

        }
        (activity as Dashboard).toolbar?.menu?.findItem(R.id.shareLink)?.setVisible(true)

        (activity as Dashboard).toolbar.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.shareLink-> {

                    val share = Intent(Intent.ACTION_SEND)
                    share.type = "text/plain"
                    share.putExtra(Intent.EXTRA_SUBJECT, "Link for the Server")
                    share.putExtra(Intent.EXTRA_TEXT, joinID)
                    startActivity(Intent.createChooser(share, "Share link!"))
                    findNavController().navigate(R.id.action_insideFragment_to_existing)
                    findNavController().navigate(R.id.action_existing_to_insideFragment)
                    return@setOnMenuItemClickListener true
                }
                else->{
                    return@setOnMenuItemClickListener true
                }

            }
        }


        subscribeToChannel()

    }




    private fun subscribeToChannel(){

        val channel = fireStore.collection("servers").document(joinID).collection("messages")

                channel.orderBy("createdAt",Query.Direction.DESCENDING).addSnapshotListener { querySnapshot, firebaseFirestoreException ->
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
        Log.d("tabrez",txt)
        val message = hashMapOf(
            "text" to txt,
            "UID" to auth.uid,
            "createdAt" to time

        )
        fireStore.collection("servers").document(joinID).collection("messages").document().set(message).addOnFailureListener {
            Log.d("tabrez","HEyyY")

        }.addOnSuccessListener {
            Log.d("tabrez","HEllooo")

        }
    }

    private fun setUpRV() {
        adapter = InsideAdap()
        chatRV.layoutManager = LinearLayoutManager(activity)
      chatRV.adapter = adapter

  }

//    class ChatFromItem : Item<ViewHolder>(){
//        override fun getLayout(): Int {
//            return R.layout.chat_from
//        }
//
//        override fun bind(viewHolder: ViewHolder, position: Int) {
//        }
//
//    }
//
//    class ChatToItem(val text : String) : Item<ViewHolder>(){
//        override fun getLayout(): Int {
//            return R.layout.chat_to
//        }
//
//        override fun bind(viewHolder: ViewHolder, position: Int) {
//            viewHolder.itemView.apply {
//                chatToTV.text = text
//            }
//        }
//
//    }




    override fun onResume() {
        super.onResume()
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        Log.d("poda","chillman")

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