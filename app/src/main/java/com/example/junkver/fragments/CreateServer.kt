package com.example.junkver.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
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
import com.example.junkver.util.Constants.Companion.topic
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_create_server.*
import kotlinx.android.synthetic.main.fragment_f_signup.*


class CreateServer : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_server, container, false)
    }
    lateinit var fireStore : FirebaseFirestore

    var selectUri : Uri ?= null
    lateinit var storageRef : FirebaseStorage
    lateinit var auth : FirebaseAuth
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        storageRef = FirebaseStorage.getInstance()

        createPhotoB.alpha = 1f
        hidebar()
        auth = FirebaseAuth.getInstance()
        fireStore = FirebaseFirestore.getInstance()
        createbutton.setOnClickListener {
            showbar()
            val view = activity?.currentFocus
            view?.let { v ->
                val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(v.windowToken, 0)
            }
            val servername = createtv.text.toString()
            if(servername.isNotEmpty()){
                createServer(servername)
            }
            else{
                hidebar()
                createtv.error = "Invalid"
            }
        }

        creategoback.setOnClickListener {
            findNavController().navigate(R.id.action_createServer_to_server)
        }
        createPhotoB.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            startActivityForResult(Intent.createChooser(intent, "Select image"),5);
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if(requestCode == 5 && resultCode == Activity.RESULT_OK && data != null ){
            selectUri = data.data
            createPhotoB.alpha = 0f
            val bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, selectUri)
            imageView4.setImageBitmap(bitmap)

        }
    }

    private fun createServer(servername : String){
        var admins = mutableListOf<String>()
        val time = java.sql.Timestamp(System.currentTimeMillis())
        val admin = auth.uid
        admin?.let { admins.add(it) }


        val server = fireStore.collection("servers").document()
        val ref = server.id
        val lastjoin = auth.currentUser?.displayName + " created the server"
        val user = hashMapOf(
            "SID" to servername ,
            "createdAt" to time,
            "Admin" to admins,
            "joinID" to ref,
            "Last" to lastjoin,
            "serverUri" to ""

        )
        val sharedPref = activity?.getSharedPreferences("notificationPref",Context.MODE_PRIVATE)

        server.set(user).addOnSuccessListener {
            FirebaseMessaging.getInstance().subscribeToTopic(topic+ref)
            with(sharedPref?.edit()){
                this?.putInt(ref,1)
                this?.apply()
            }
            Toast.makeText(activity,"Created Successfully",Toast.LENGTH_SHORT).show()
            uploadGroupImage(server)
            findNavController().navigate(R.id.action_createServer_to_existing)
            hidebar()
        }.addOnFailureListener {
            hidebar()
            Toast.makeText(activity,"UnSuccessfull Try again",Toast.LENGTH_SHORT).show()

        }.addOnCanceledListener {
            hidebar()
            Toast.makeText(activity,"Error!",Toast.LENGTH_SHORT).show()

        }
    }



    private fun uploadGroupImage(server : DocumentReference){

        val imageRef = storageRef.reference.child(server.id)
        selectUri?.let { 
            val ref = imageRef
            ref.putFile(it).addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener {download->
                    server.update("serverUri",download.toString())

                }
                }
        }
    }


    private fun showbar(){
        createBar.visibility = View.VISIBLE
        createbutton.isEnabled = false

    }

    private fun hidebar(){
        createBar.visibility = View.INVISIBLE
        createbutton.isEnabled = true
    }





}
