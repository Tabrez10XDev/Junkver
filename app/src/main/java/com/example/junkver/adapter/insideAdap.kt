package com.example.junkver.adapter

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.junkver.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.chat_from.view.*
import kotlinx.android.synthetic.main.chat_to.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs


class InsideAdap : RecyclerView.Adapter<InsideAdap.ToViewHolder>() {
    private val auth = FirebaseAuth.getInstance()

    inner class ToViewHolder(itemView : View): RecyclerView.ViewHolder(itemView)



    private val differCallback = object : DiffUtil.ItemCallback<Map<String,Any>>(){




        override fun areItemsTheSame(
            oldItem: Map<String, Any>,
            newItem: Map<String, Any>
        ): Boolean {
            return oldItem.get("text") == newItem.get("text")
        }

        override fun areContentsTheSame(
            oldItem: Map<String, Any>,
            newItem: Map<String, Any>
        ): Boolean {
            return oldItem == newItem
        }

    }
    var fireStore : FirebaseFirestore = FirebaseFirestore.getInstance()

    val differ2 = AsyncListDiffer(this,differCallback)




    override fun getItemCount(): Int {
        return differ2.currentList.size
    }


    private var onItemClickListener : ((Map<String,Any>) -> Unit)?= null

    fun setOnItemClickListener(listener:(Map<String,Any>)->Unit){
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): InsideAdap.ToViewHolder {
        Log.d("shami",viewType.toString())
        when(viewType){
            12->{
                return ToViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.chat_from,parent,false ))

            }
            else->{
                return ToViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.chat_to,parent,false ))

            }
        }
    }

    override fun onBindViewHolder(holder: InsideAdap.ToViewHolder, position: Int) {
        val txt = differ2.currentList[position]
        val formatter = SimpleDateFormat("HH:mm")
        val dateFormat = SimpleDateFormat("dd:MM:yyyy")

        var time = formatter.format(txt.get("createdAt"))
        val dat = dateFormat.format(txt.get("createdAt"))

        val date = dateFormat.parse(dat)
        val currentdat = dateFormat.format(Date())
        val currentdate = dateFormat.parse(currentdat)
        val diff: Long = abs(date.time - currentdate.time)
        var day = diff/(24*60*60*1000)
        if(day > 1){
            time = dat
        }
        when(holder.itemViewType){
            11->{
                holder.itemView.apply {
                    chatToTV.text = txt.get("text").toString()
                    chatToName.text = txt.get("username").toString()
                    chatToTime.text = time
                    fireStore.collection("persons").document(txt.get("UID").toString()).get().addOnSuccessListener {
                        val selectUri = Uri.parse(it.get("photoUri").toString())
                        Glide.with(this).load(selectUri).into(chattoMV)

                    }
                }
            }
            12->{
                holder.itemView.apply {
                    chatTV.text = txt.get("text").toString()
                    chatName.text = txt.get("username").toString()
                    chatTime.text = time
                    fireStore.collection("persons").document(txt.get("UID").toString()).get().addOnSuccessListener {
                        val selectUri = Uri.parse(it.get("photoUri").toString())
                        Glide.with(this).load(selectUri).into(chatMV)

                    }
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {

        when(differ2.currentList[position].get("UID").toString()){
            auth.uid->{

                return 11
            }
            else->{
                return 12
            }
        }
        return super.getItemViewType(position)


    }

}