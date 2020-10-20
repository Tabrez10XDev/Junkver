package com.example.junkver.VM

//import android.util.Log
//import android.widget.Toast
//import androidx.lifecycle.ViewModel
//import com.example.junkver.adapter.existingAdap
//import com.example.junkver.data.Person
//import com.google.firebase.firestore.FirebaseFirestore
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
//    }
//
//    private fun retrievePersons() = CoroutineScope(Dispatchers.IO).launch {
//        try {
//            val sb : MutableList<Map<String,Any>> = arrayListOf()
//            val querySnapshot = fireStore.collection("persons").get()
//            for(document in querySnapshot.result!!){
//                val person = document.toObject<Person::class.java>()
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
//}