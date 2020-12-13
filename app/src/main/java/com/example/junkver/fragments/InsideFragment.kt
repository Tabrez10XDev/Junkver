package com.example.junkver.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
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
import com.example.junkver.util.Constants
import com.example.junkver.util.Constants.Companion.topic
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.squareup.okhttp.Dispatcher
import kotlinx.android.synthetic.main.dashboard_bar.*
import kotlinx.android.synthetic.main.fragment_inside.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.internal.notify

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


    val sharedPref = activity?.getSharedPreferences("notificationPref",Context.MODE_PRIVATE)

        fireStore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        val username =  auth.currentUser?.displayName

        setUpRV()

        (activity as Dashboard).toolbar?.menu?.findItem(R.id.shareLink)?.setVisible(true)
        (activity as Dashboard).toolbar?.menu?.findItem(R.id.notify)?.setVisible(true)
        var pref = sharedPref?.getInt(joinID,1)

        Log.d("Response","initial"+pref.toString())



        pref?.let { modityNotifications(it) }




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
                        txt.toString(),
                        username.toString(),
                        joinID
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


        (activity as Dashboard).toolbar.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.shareLink-> {
                    var clipboard = (activity as Dashboard).getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    var clip = ClipData.newPlainText("label",joinID)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(activity,"Server ID copied to your clipboard, paste it in JoinID",Toast.LENGTH_LONG).show()
                    return@setOnMenuItemClickListener true
                }
                R.id.notify->{
                    if(pref!= null){
                        Log.d("Response","if")

                        if(pref == 1) {
                            Toast.makeText(activity, "Turned off notifications", Toast.LENGTH_SHORT).show()
                            pref = 0
                            with(sharedPref?.edit()){
                                this?.putInt(joinID,0)
                                this?.apply()
                            }
                            modityNotifications(pref!!)
                        }
                        else{
                            Toast.makeText(activity, "Turned on notifications", Toast.LENGTH_SHORT).show()
                            pref = 1
                            with(sharedPref?.edit()){
                                this?.putInt(joinID,1)
                                this?.apply()
                            }
                            modityNotifications(pref!!)
                        }
                    }
                    else{
                        Log.d("Response","else")

                        with(sharedPref?.edit()){
                            this?.putInt(joinID,1)
                            this?.apply()
                        }
                    }

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
        Log.d("adjust","adjusting")
        chatRV.postDelayed(Runnable {
            chatRV.scrollToPosition(chatRV.adapter!!.itemCount - 1)
        },100)
    }


    private fun subscribeToChannel(){

        val channel = fireStore.collection("servers").document(joinID).collection("messages")
        val sharedPref = activity?.getSharedPreferences("notificationPref",Context.MODE_PRIVATE)

                channel.orderBy("createdAt",Query.Direction.ASCENDING).addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                firebaseFirestoreException?.let {
                    return@addSnapshotListener
                }
                    var final : Map<String,Any> ?= null

                    querySnapshot?.let {documents->
                    val sb : MutableList<Map<String,Any>> = arrayListOf()

                    for(document in documents) {
                        sb.add(document.data)
                        final = document.data

                    }
                    adapter.differ2.submitList(sb)
                }
                    var person = final?.get("username")
                    val currentID = sharedPref?.getString("currentID","")
                    if(currentID != "" && person != auth.currentUser?.displayName){
                    adjustRV()
            }   }



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
                fireStore.collection("servers").document(joinID).update("Last",auth.currentUser?.displayName + ": "+txt)

            }
    }

    private fun setUpRV() {
        adapter = InsideAdap()
        val manager = LinearLayoutManager(activity)
        manager.stackFromEnd = true
        chatRV.layoutManager = manager
      chatRV.adapter = adapter
  }

    override fun onPause() {
        super.onPause()
        val sharedPref = activity?.getSharedPreferences("notificationPref",Context.MODE_PRIVATE)
        with(sharedPref?.edit()){
            this?.putString("currentID","")
            this?.apply()
        }
    }

    override fun onResume() {
        super.onResume()
        val sharedPref = activity?.getSharedPreferences("notificationPref",Context.MODE_PRIVATE)
        val unseenPref = activity?.getSharedPreferences("unseenPref",Context.MODE_PRIVATE)
        val joinID = arguments?.getString("joinID").toString()
        with(sharedPref?.edit()){
            this?.putString("currentID",joinID)
            this?.apply()
        }
        with(unseenPref?.edit()){
            this?.putInt(joinID,0)
            this?.apply()
        }
    }




    private fun modityNotifications(pref : Int){
        if(pref != null) {
            val notify = (activity as Dashboard).toolbar.menu.findItem(R.id.notify)

            if (pref == 1) {
                FirebaseMessaging.getInstance().subscribeToTopic(Constants.topic + joinID).addOnSuccessListener {
                    Log.d("Response","OSTHI")
                }
                notify.setIcon(R.drawable.ic_baseline_notifications_active_24)

            } else {
                notify.setIcon(R.drawable.ic_baseline_notifications_off_24)



            }
        }
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