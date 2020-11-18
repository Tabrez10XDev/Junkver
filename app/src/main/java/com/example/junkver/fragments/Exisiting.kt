package com.example.junkver.fragments

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.junkver.R
import com.example.junkver.adapter.existingAdap
import com.example.junkver.app.Dashboard
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.dashboard_bar.*
import kotlinx.android.synthetic.main.fragment_existing.*
import kotlinx.coroutines.*
import java.lang.Runnable

class Exisiting : Fragment() {
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
//    private val hideRunnable = Runnable { hide() }


    private var fullscreenContent: View? = null
    private var fullscreenContentControls: View? = null

    lateinit var auth : FirebaseAuth
    lateinit var existingAdap: existingAdap
    lateinit var fireStore : FirebaseFirestore
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_existing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        fireStore = FirebaseFirestore.getInstance()
        val col = fireStore.collection("persons")
        visible = true
        setUpRV()
        existingAdap.setOnItemClickListener {
            val bundle = Bundle().apply {
                putString("SID",it.get("SID").toString())
                putString("joinID",it.get("joinID").toString())
            }
            (activity as Dashboard).num = 1
            findNavController().navigate(R.id.action_existing_to_insideFragment,bundle)

//            val intent = Intent(activity,ChatActivity::class.java)
//            intent.putExtra("bundle",bundle)
//            startActivity(intent)

        }
        auth = FirebaseAuth.getInstance()

        fullscreenContent = view.findViewById(R.id.fullscreen_content)
        fullscreenContentControls = view.findViewById(R.id.fullscreen_content_controls)
//        retrievePersons()

        subscribeToServers()

    }

    private fun subscribeToPersons(){

        fireStore.collection("persons").orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                firebaseFirestoreException?.let {
                    return@addSnapshotListener
                }
                querySnapshot?.let {documents->
                    val sb : MutableList<Map<String,Any>> = arrayListOf()

                    for(document in documents) {
                        sb.add(document.data)
                    }
                    Log.d("lush",sb.toString())

                    existingAdap.differ.submitList(sb)
                    existingAdap.notifyDataSetChanged()
                }
            }
    }


    private fun subscribeToServers(){


       val fire = fireStore.collection("servers").whereArrayContains("Admin",auth.uid.toString()).orderBy("createdAt", Query.Direction.DESCENDING)
        fire.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                firebaseFirestoreException?.let {
                    Log.d("kingsman",it.toString())

                    return@addSnapshotListener
                }

            querySnapshot?.let {documents->
                    val sb : MutableList<Map<String,Any>> = arrayListOf()

                    for(document in documents) {
                        sb.add(document.data)

                    }

                    existingAdap.differ.submitList(sb)
                    existingAdap.notifyDataSetChanged()
                }
            }
    }

//    private fun retrievePersons() = CoroutineScope(Dispatchers.IO).launch {
//        try {
//            val sb : MutableList<Map<String,Any>> = arrayListOf()
//            val querySnapshot = fireStore.collection("persons").get().addOnFailureListener() {
//                Log.d("tabby", it.message)
//
//            }.addOnSuccessListener {documents->
//                for(document in documents) {
//                    Log.d("low", "${document.id} => ${document.data}")
//                    sb.add(document.data)
//                }
//                existingAdap.differ.submitList(sb)
//            }
//
//        }
//        catch (e : Exception){
//            withContext(Dispatchers.Main){
//                Toast.makeText(activity,"Error",Toast.LENGTH_LONG).show()
//
//            }
//        }
//    }

//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        inflater.inflate(R.menu.inside_menu, menu)
//        val act = menu.findItem(R.id.shareLink)
//        act.setVisible(true)
//        super.onCreateOptionsMenu(menu, inflater)
//    }

    private fun setUpRV(){
        existingAdap = existingAdap()
        rvExisting.apply {
            adapter = existingAdap
            layoutManager = LinearLayoutManager(activity)
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        (activity as Dashboard).toolbar?.menu?.findItem(R.id.shareLink)?.setVisible(false)


//        delayedHide(100)

    }

    override fun onPause() {
        super.onPause()
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        activity?.window?.decorView?.systemUiVisibility = 0
//        show()
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

//    @Suppress("InlinedApi")
//    private fun show() {
//        fullscreenContent?.systemUiVisibility =
//            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
//                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//        visible = true
//
//        hideHandler.removeCallbacks(hidePart2Runnable)
//        hideHandler.postDelayed(showPart2Runnable, UI_ANIMATION_DELAY.toLong())
//        (activity as? AppCompatActivity)?.supportActionBar?.show()
//    }

//
//    private fun delayedHide(delayMillis: Int) {
//        hideHandler.removeCallbacks(hideRunnable)
//        hideHandler.postDelayed(hideRunnable, delayMillis.toLong())
//    }

    companion object {

        private const val AUTO_HIDE = true

        private const val AUTO_HIDE_DELAY_MILLIS = 3000


        private const val UI_ANIMATION_DELAY = 300
    }
}