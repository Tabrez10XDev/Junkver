package com.example.junkver.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
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
import com.example.junkver.app.Dashboard
import com.example.junkver.util.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.fragment_f_signup.*
import kotlinx.android.synthetic.main.fragment_server.*

class Server : Fragment() {



    lateinit var fireStore : FirebaseFirestore
    lateinit var auth : FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_server, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as Dashboard).num = 3

        hidebar()
        fireStore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        joinFAB.setOnClickListener {
            findNavController().navigate(R.id.action_server_to_createServer)
        }
        joinbutton.setOnClickListener {
            showbar()
            val servername = jointv.text.toString()
            val view = activity?.currentFocus
            view?.let { v ->
                val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(v.windowToken, 0)
            }
            if(servername.isNotEmpty()){
                joinServer(servername)
            }
            else{
                hidebar()
                jointv.error = "Invalid"
            }
        }

    }


    private fun showbar(){
        joinBar.visibility = View.VISIBLE
        joinbutton.isEnabled = false

    }

    private fun hidebar(){
        joinBar.visibility = View.INVISIBLE
        joinbutton.isEnabled = true
    }

    private fun joinServer(servername : String) {
        val time = java.sql.Timestamp(System.currentTimeMillis())
        val server =
            fireStore.collection("servers").document(servername).get().addOnSuccessListener {
                try{
                var admins = mutableListOf<String>()
                Log.d("final","two")

                val data = it.data
                val admin = data?.get("Admin")
                for(i in admin as List<String>){
                    admins.add(i)
                    Log.d("final","loop")

                }
                auth.uid?.let { it1 -> admins.add(it1) }
                Log.d("final","three")

                Log.d("final",admins.toString())
                fireStore.collection("servers").document(servername).update("Admin", admins)
                    .addOnSuccessListener {
                        FirebaseMessaging.getInstance().subscribeToTopic(Constants.topic+servername)

                        hidebar()
                        fireStore.collection("servers").document(servername)
                            .update("createdAt", time).addOnSuccessListener {
                                Toast.makeText(activity, "Success", Toast.LENGTH_SHORT).show()
                            }.addOnFailureListener {
                                Log.d("final",it.message)

                            }

                    }.addOnFailureListener {
                        Log.d("final",it.message)

                    }
}
                catch (e : Exception){
                    hidebar()
                    Toast.makeText(activity,"Invalid",Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Log.d("final",it.message)
            }
    }


}


