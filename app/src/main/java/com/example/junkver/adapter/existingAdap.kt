package com.example.junkver.adapter

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.junkver.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.chatlist_ui.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

class existingAdap :RecyclerView.Adapter<existingAdap.ArticleViewHolder>() {

    inner class ArticleViewHolder(itemView : View): RecyclerView.ViewHolder(itemView)



    private val differCallback = object : DiffUtil.ItemCallback<Map<String,Any>>(){




        override fun areItemsTheSame(
            oldItem: Map<String, Any>,
            newItem: Map<String, Any>
        ): Boolean {
            return oldItem.get("UID") == newItem.get("UID")
        }

        override fun areContentsTheSame(
            oldItem: Map<String, Any>,
            newItem: Map<String, Any>
        ): Boolean {
return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this,differCallback)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        return ArticleViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.chatlist_ui,parent,false ))
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article = differ.currentList[position]
        holder.itemView.apply {
//            Glide.with(this).load(article.urlToImage).into(ivArticleImage)
//            tvSource.text= article.source?.name
//            tvTitle.text = article.title
//            tvDescription.text = article.description
//            tvPublishedAt.text = article.publishedAt
            setOnClickListener {
                onItemClickListener?.let { it(article) }
            }
            val formatter = SimpleDateFormat("HH:mm")
            val dateFormat = SimpleDateFormat("dd:MM:yyyy")
            val time = formatter.format(article.get("createdAt"))
            val dat = dateFormat.format(article.get("createdAt"))
            val date = dateFormat.parse(dat)
            val currentdat = dateFormat.format(Date())
            val currentdate = dateFormat.parse(currentdat)
            chatlisthead.text = article.get("SID").toString()
            chatlistText.text = article.get("Last").toString()
            val photoUri = Uri.parse(article.get("serverUri").toString())
            Log.d("photo",photoUri.toString())
            Glide.with(this).load(photoUri).into(chatlistphoto)
            val diff: Long = abs(date.time - currentdate.time)
            var day = diff/(24*60*60*1000)
            if(day < 1 ) {
                chatTime.text = time.toString()
            }
            else if(day.toInt() == 1){
                chatTime.text = "Yesterday"
            }
            else if(day > 1 && day < 7){
                chatTime.text = day.toString() + " day"
            }
            else{
                day = day/7
                chatTime.text = day.toString() + " week"

            }
        }


    }

    private var onItemClickListener : ((Map<String,Any>) -> Unit)?= null

    fun setOnItemClickListener(listener:(Map<String,Any>)->Unit){
        onItemClickListener = listener
    }

}
