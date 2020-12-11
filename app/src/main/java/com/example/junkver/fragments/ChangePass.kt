package com.example.junkver.fragments

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.junkver.R
import com.example.junkver.app.Dashboard
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_change_pass.*
import kotlinx.android.synthetic.main.fragment_f_signup.*
import java.lang.Exception

class ChangePass : Fragment() {



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_change_pass, container, false)
    }

    lateinit var auth : FirebaseAuth
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hidebar()

        (activity as Dashboard).num = 3

        auth = FirebaseAuth.getInstance()


        buttonpass.setOnClickListener {
            updatePass()
        }



    }


    private fun updatePass(){
        showbar()
        val passone = pass1.text.toString()
        val passtwo = pass2.text.toString()
        if(passone.isNotEmpty() && passtwo.isNotEmpty() && passone==passtwo){
            try {
                auth.currentUser.let {
                    auth.currentUser!!.updatePassword(passone).addOnSuccessListener {
                        hidebar()
                        Toast.makeText(activity,"Successful",Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        hidebar()
                        Toast.makeText(activity, "Unsuccessful try again", Toast.LENGTH_SHORT).show()
                    }.addOnCanceledListener {
                            hidebar()
                            Toast.makeText(activity,"Unsuccessful try again",Toast.LENGTH_SHORT).show()
                        }
                    }



            }
            catch (e : Exception){
                hidebar()
                Toast.makeText(activity,e.message,Toast.LENGTH_SHORT).show()
            }
        }
        else{
            hidebar()
            Toast.makeText(activity,"Invalid Credentials",Toast.LENGTH_SHORT).show()
        }
    }

    private fun showbar(){
        progresspass.visibility = View.VISIBLE
        buttonpass.isEnabled = false

    }

    private fun hidebar(){
        progresspass.visibility = View.INVISIBLE
        buttonpass.isEnabled = true
    }





}