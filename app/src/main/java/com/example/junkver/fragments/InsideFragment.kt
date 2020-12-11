package com.example.junkver.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.marginBottom
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.junkver.R
import com.example.junkver.adapter.InsideAdap
import com.example.junkver.app.Dashboard
import com.example.junkver.data.NotificationData
import com.example.junkver.data.PushNotification
import com.example.junkver.data.RetrofitInstance
import com.example.junkver.util.Constants.Companion.topic
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import com.squareup.okhttp.Dispatcher
import kotlinx.android.synthetic.main.dashboard_bar.*
import kotlinx.android.synthetic.main.fragment_inside.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val TOPIC = "/topics/myTopic"


class InsideFragment : Fragment() {




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

        val sid = arguments?.getString("SID")
        val serverUri = arguments?.getString("serverUri")
        joinID = arguments?.getString("joinID").toString()
        (activity as Dashboard).toolbar.title = sid


        fireStore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val username =  auth.currentUser?.displayName

        setUpRV()



        (activity as Dashboard).toolbar.setOnClickListener {
            (activity as Dashboard).num = 2
            val bundle = Bundle().apply {
                putString("joinID",joinID)
                putString("SID",sid)
                putString("serverUri",serverUri)
            }
            (activity as Dashboard).joinID = joinID
            (activity as Dashboard).SID = sid.toString()
            findNavController().navigate(R.id.action_insideFragment_to_groupInfo,bundle)
        }

        sendText.setOnClickListener {
            (activity as Dashboard).clicked = true
        }

        sendbutton.setOnClickListener {

            val txt = sendText.text
            if(txt.isNotEmpty() and hasInternetConnection()){
                PushNotification(
                    NotificationData(
                        sid.toString(),
                        txt.toString()
//                        username.toString()
                    ),
                    topic+joinID
//                        TOPIC
                ).also {
                    sendNotification(it)
                }
                sendMessage()
                adjustRV()
            }
            else if(!hasInternetConnection()){
                Toast.makeText(activity,"Check your Internet",Toast.LENGTH_SHORT).show()
            }
        }
        (activity as Dashboard).toolbar?.menu?.findItem(R.id.shareLink)?.setVisible(true)
        (activity as Dashboard).toolbar?.menu?.findItem(R.id.notificationBtn)?.setVisible(true)


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





        subscribeToChannel()



    }


    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try{
            val response = RetrofitInstance.api.postNotification(notification)

            if(response.isSuccessful){
                Log.d("Response", "lk"+response.toString())
            }
            else{
                Log.d("Response","heyy"+ response.raw())

            }
        }
        catch(e : Exception){
            Log.d("Response","kj"+e.message)
            Toast.makeText(activity,e.message,Toast.LENGTH_LONG).show()
        }

    }

    private fun adjustRV(){
        chatRV.postDelayed(Runnable {
            chatRV.scrollToPosition(chatRV.adapter!!.itemCount - 1)
        },100)
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





    private fun hasInternetConnection(): Boolean{
        val connectivityManager = activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            val activeNetworks = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetworks) ?: return false
            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)-> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)->true
                else -> false
            }
        }
        else{
            connectivityManager.activeNetworkInfo?.run {
                return when(type){
                    ConnectivityManager.TYPE_WIFI -> true
                    ConnectivityManager.TYPE_MOBILE ->true
                    ConnectivityManager.TYPE_ETHERNET ->true
                    else -> false
                }

            }
        }
        return false
    }





}