package com.example.junkver.fragments

import android.app.Activity
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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.junkver.R
import com.example.junkver.app.Dashboard
import com.example.junkver.app.Login
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class Profile : Fragment() {

    lateinit var fireStore : FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    var selecturi : Uri ?= null
    lateinit var auth : FirebaseAuth
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        fireStore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        selecturi = (activity as Dashboard).selectUri
        hidebar()

        if(selecturi != null){
            profbutton.alpha = 0f
            profphoto.alpha = 1f

        }
        Glide.with(this).load(selecturi).into(profphoto)
//        subscribeToDp()
        Log.d("shameer","shamiii")
        val email = auth.currentUser?.email
        profmail.setText(email)
        profuser.setText(auth.currentUser?.displayName)

        profbutton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            startActivityForResult(Intent.createChooser(intent, "Select image"),2);
        }

        profupdate.setOnClickListener {
            showbar()
            updateProfile()
        }

        proflogout.setOnClickListener{
            auth.signOut()
            startActivity(Intent(activity,Login::class.java))
            activity?.finish()
        }




    }




    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if(requestCode == 2 && resultCode == Activity.RESULT_OK && data != null ){
            selecturi = data.data
            profbutton.alpha = 0f
            Glide.with(this).load(selecturi).into(profphoto)


        }
    }

//    private fun subscribeToDp(){
//
//        val channel = fireStore.collection("persons").document(auth.uid.toString()).addSnapshotListener {
//                documentSnapshot, firebaseFirestoreException ->
//
//            firebaseFirestoreException?.let {
//
//                Log.d("shameer",it.message)
//                return@addSnapshotListener
//            }
//
//            documentSnapshot?.let {document->
//                val data = document.data?.get("photoUri").toString()
//                Log.d("shameer","help "+data)
//
//                selecturi = Uri.parse(data)
//                if(selecturi!= null){
//                    profbutton.alpha = 0f
//                }
//                hidebar()
//
//                Glide.with(this).load(selecturi).into(profphoto)
//
//
//
//            }
//        }
//
//    }

    private fun showbar(){
        profbar.visibility = View.VISIBLE
        profupdate.isEnabled = false

    }

    private fun hidebar(){
      profbar.visibility = View.INVISIBLE
      profupdate.isEnabled = true
    }
   lateinit var profileUpdate : UserProfileChangeRequest

    private fun updateProfile() {
        val username = profuser.text.toString()

        if (username.isNotEmpty()){
            auth.currentUser?.let { user ->
                if(selecturi != null) {
                    profileUpdate = UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .setPhotoUri(selecturi)
                        .build()
                }
                else{
                    profileUpdate = UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build()
                }
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        user.updateProfile(profileUpdate).addOnSuccessListener {
                            hidebar()

                            val navView: NavigationView ?= activity?.findViewById(R.id.nav_view)

                            var swipe = navView?.getHeaderView(0)
                            var swipename = swipe?.findViewById<TextView>(R.id.swipename)
                            swipename?.text = username
                            if(selecturi!= null) {
                                var swipephoto =
                                    swipe!!.findViewById<CircleImageView>(R.id.swipephoto)

                                Glide.with(this@Profile).load(selecturi).into(swipephoto)

                                Toast.makeText(activity, "Updated username", Toast.LENGTH_SHORT)
                                    .show()
                            }
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



}