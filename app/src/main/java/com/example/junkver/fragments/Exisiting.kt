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
        (activity as Dashboard).toolbar.setTitle("Junkver")

        setUpRV()
        existingAdap.setOnItemClickListener {
            val bundle = Bundle().apply {
                putString("SID",it.get("SID").toString())
                putString("joinID",it.get("joinID").toString())
            }
            (activity as Dashboard).num = 1
            findNavController().navigate(R.id.action_existing_to_insideFragment,bundle)



        }
        auth = FirebaseAuth.getInstance()



        subscribeToServers()

    }

//    private fun subscribeToPersons(){
//
//        fireStore.collection("persons").orderBy("createdAt", Query.Direction.DESCENDING)
//            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
//                firebaseFirestoreException?.let {
//                    return@addSnapshotListener
//                }
//                querySnapshot?.let {documents->
//                    val sb : MutableList<Map<String,Any>> = arrayListOf()
//
//                    for(document in documents) {
//                        sb.add(document.data)
//                    }
//                    Log.d("lush",sb.toString())
//
//                    existingAdap.differ.submitList(sb)
//                    existingAdap.notifyDataSetChanged()
//                }
//            }
//    }


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




}