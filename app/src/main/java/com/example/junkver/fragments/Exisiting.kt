package com.example.junkver.fragments

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.junkver.R
import com.example.junkver.adapter.existingAdap
import com.example.junkver.app.Dashboard
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import kotlinx.android.synthetic.main.fragment_existing.*
import kotlinx.coroutines.*
import java.lang.Runnable
import java.lang.StringBuilder

/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
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
    private val hideRunnable = Runnable { hide() }


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
//            val bundle = Bundle().apply {
//                putSerializable("article",it)
//            }    DO LATER
            findNavController().navigate(R.id.action_existing_to_insideFragment)
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

        fireStore.collection("servers").whereEqualTo("Admin",auth.uid).orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                firebaseFirestoreException?.let {
                    return@addSnapshotListener
                }
                querySnapshot?.let {documents->
                    val sb : MutableList<Map<String,Any>> = arrayListOf()

                    for(document in documents) {
                        sb.add(document.data)
                    }
                    Log.d("king",sb.toString())

                    existingAdap.differ.submitList(sb)
                    existingAdap.notifyDataSetChanged()
                }
            }
    }

    private fun retrievePersons() = CoroutineScope(Dispatchers.IO).launch {
        try {
            val sb : MutableList<Map<String,Any>> = arrayListOf()
            val querySnapshot = fireStore.collection("persons").get().addOnFailureListener() {
                Log.d("tabby", it.message)

            }.addOnSuccessListener {documents->
                for(document in documents) {
                    Log.d("low", "${document.id} => ${document.data}")
                    sb.add(document.data)
                }
                existingAdap.differ.submitList(sb)
            }

        }
        catch (e : Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(activity,"Error",Toast.LENGTH_LONG).show()

            }
        }
    }

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