package com.example.junkver.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.junkver.R
import com.example.junkver.app.Dashboard
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_f_login.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class FLogin : Fragment() {



    override fun onStart() {
        super.onStart()
        checkLoggedInState()
    }

    lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_f_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        hidebar()


                blogin.setOnClickListener {

                    hideKeyboard()
                    loginUser()
                }

        bsignup.setOnClickListener {
         hideKeyboard()
            findNavController().navigate(R.id.action_FLogin_to_FSignup)

        }
    }

    private fun hideKeyboard(){
        val view = activity?.currentFocus
        view?.let { v ->
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }



    private fun loginUser() {
        showbar()
        val email = tvsign.text.toString()
        val password = tvpass.text.toString()
        if( email.isNotEmpty() && password.isNotEmpty()){
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    auth.signInWithEmailAndPassword(email,password).addOnSuccessListener {
                        hidebar()
                        checkLoggedInState()
                    }.addOnFailureListener {
                            hidebar()
                            Toast.makeText(activity, "Invalid Credentials", Toast.LENGTH_SHORT).show()

                    }.addOnCanceledListener {
                        hidebar()
                        Toast.makeText(activity, "Error!", Toast.LENGTH_SHORT).show()

                    }

                }
                catch (e : Exception){
                    withContext(Dispatchers.Main){
                        hidebar()
                        Toast.makeText(activity,e.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }

        }
        else{
            Toast.makeText(activity,"Invalid Credentials", Toast.LENGTH_SHORT).show()
            hidebar()
        }
    }

        private fun checkLoggedInState(){
        if(auth.currentUser != null){
            Toast.makeText(activity,"Logged in as " + auth.currentUser?.displayName, Toast.LENGTH_SHORT).show()
            val intent = Intent(activity, Dashboard::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            activity?.finish()

        }
    }

    private fun hidebar(){
        progresslog.visibility = View.INVISIBLE
        blogin.isEnabled = true
        bsignup.isEnabled = true
    }

    private fun showbar(){
        progresslog.visibility = View.VISIBLE
        blogin.isEnabled = false
        bsignup.isEnabled = false
    }


    override fun onResume() {
        super.onResume()
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

    }

    override fun onPause() {
        super.onPause()
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

    }











}