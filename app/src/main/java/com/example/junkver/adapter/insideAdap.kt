package com.example.junkver.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.junkver.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.chat_from.view.*
import kotlinx.android.synthetic.main.chat_to.view.*


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

        when(holder.itemViewType){
            11->{
                holder.itemView.apply {
                    chatToTV.text = txt.get("text").toString()
                    chatToName.text = txt.get("username").toString()
                }
            }
            12->{
                holder.itemView.apply {
                    chatTV.text = txt.get("text").toString()
                    chatName.text = txt.get("username").toString()
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {

        Log.d("shami",differ2.currentList[position].get("UID").toString())
        when(differ2.currentList[position].get("UID").toString()){
            auth.uid->{
                Log.d("shami",differ2.currentList[position].get("UID").toString())

                return 11
            }
            else->{
                return 12
            }
        }
        return super.getItemViewType(position)


    }

}