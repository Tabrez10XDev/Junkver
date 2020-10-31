package com.example.junkver.VM
//
//import android.util.Log
//import android.widget.Toast
//import androidx.lifecycle.ViewModel
//import com.example.junkver.adapter.existingAdap
//import com.example.junkver.data.Person
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.firestore.Query
//import com.google.firebase.firestore.QueryDocumentSnapshot
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import java.lang.StringBuilder
//
//class DashboardVM () : ViewModel(){
//    val fireStore = FirebaseFirestore.getInstance()
//    init {
//
//        retrievePersons()
//
//    }
//    var cb : MutableMap<String, Any> = mutableMapOf()
//
//    private fun retrievePersons() = CoroutineScope(Dispatchers.IO).launch {
//        fireStore.collection("persons")
//            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
//                firebaseFirestoreException?.let {
//                    return@addSnapshotListener
//                }
//                querySnapshot?.let {documents->
//                    val sb : MutableList<Map<String,Any>> = arrayListOf()
//
//                    for(document in documents) {
//                        sb.add(document.data)
//                        document.data.get("username")?.let {
//                            cb.put(document.data.get("UID").toString(),
//                                it
//                            )
//                        }
//
//                    }
//
//                }
//            }
//    }
//
//}