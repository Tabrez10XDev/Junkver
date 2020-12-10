package com.example.junkver.fragments

import android.app.Activity
import android.app.Person
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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.junkver.R
import com.example.junkver.app.Dashboard
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_f_signup.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class FSignup : Fragment() {




    lateinit var auth : FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_f_signup, container, false)
    }


    var selecturi : Uri?= null
    lateinit var storageRef : FirebaseStorage

    lateinit var fireStore : FirebaseFirestore

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        fireStore = FirebaseFirestore.getInstance()



        storageRef = FirebaseStorage.getInstance()
        hidebar()
        bphoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "image/*"
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            startActivityForResult(Intent.createChooser(intent, "Select image"),1);
        }

        bregister.setOnClickListener {
            val view = activity?.currentFocus
            view?.let { v ->
                val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(v.windowToken, 0)
            }
            registerUser()

        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if(requestCode == 1 && resultCode == Activity.RESULT_OK && data != null ){
            selecturi = data.data
            bphoto.alpha = 0f
            val bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, selecturi)
            circlesign.setImageBitmap(bitmap)

        }
    }


    private fun savePerson(username : String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val time = java.sql.Timestamp(System.currentTimeMillis())
            val user = hashMapOf(
                "UID" to auth.uid,
                "username" to username,
                "photoUri" to ""
            )
            auth.uid?.let {
                var personCollection = fireStore.collection("persons").document(auth.uid!!)
                    personCollection.set(user as Map<String, Any>).addOnFailureListener {
                }.addOnSuccessListener {
                        uploadPhoto(personCollection)
                }


            }


        }
        catch (e : Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(activity,e.message+" Update your profile again",Toast.LENGTH_LONG).show()
                Log.d("taby",e.message  )

            }
        }
    }
    private fun uploadPhoto(personCollection : DocumentReference){
        val imageRef = storageRef.reference.child(auth.uid!!)
        selecturi?.let {
              imageRef.putFile(it).addOnSuccessListener {

                  it.storage.downloadUrl.addOnSuccessListener {  uri->
                  personCollection.update("photoUri",uri.toString())

          }
              }
        }

    }

    private fun showbar(){
        progresssign.visibility = View.VISIBLE
        bregister.isEnabled = false

    }

    private fun hidebar(){
        progresssign.visibility = View.INVISIBLE
        bregister.isEnabled = true
    }
    private fun registerUser() {
        showbar()
        val email = tvsign.text.toString()
        val password = tvpass.text.toString()
        if( email.isNotEmpty() && password.isNotEmpty()){
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    auth.createUserWithEmailAndPassword(email,password).addOnSuccessListener {
                        updateProfile()
                        Toast.makeText(activity,"Account created",Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener(){
                        Toast.makeText(activity,it.message,Toast.LENGTH_SHORT).show()
                        hidebar()
                    }


                }
                catch (e : Exception){
                    withContext(Dispatchers.Main){
                        hidebar()
                        Toast.makeText(activity,e.message,Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


    private fun updateProfile() {
        val username = tvuserreg.text.toString()

        if (username.isNotEmpty() && selecturi != null ){
            auth.currentUser?.let { user ->
                val profileUpdate = UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .build()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        user.updateProfile(profileUpdate).addOnSuccessListener {

                            savePerson(username)
                            hidebar()
                            Toast.makeText(activity, "Updated username", Toast.LENGTH_SHORT).show()
                            checkLoggedInState()

                        }.addOnFailureListener {
                            hidebar()
                            Toast.makeText(activity, it.message, Toast.LENGTH_SHORT).show()

                        }.addOnCanceledListener {
                            hidebar()
                            Toast.makeText(activity,"Please Login manually and update your username", Toast.LENGTH_SHORT).show()
                        }


                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            hidebar()
                            Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
    }
        else{
            hidebar()
            Toast.makeText(activity,"Please Login manually and update your username", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkLoggedInState(){
        if(auth.currentUser == null){
        }
        else{
            Toast.makeText(activity,"Logged in as " + auth.currentUser?.displayName,Toast.LENGTH_SHORT).show()
val intent = Intent(activity, Dashboard::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)

            startActivity(intent)
            activity?.finish()
        }
    }







}