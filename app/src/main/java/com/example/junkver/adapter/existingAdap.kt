package com.example.junkver.adapter

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
import kotlinx.android.synthetic.main.chatlist_ui.view.*

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
            chatlisthead.text = article.get("SID").toString()
        }


    }

    private var onItemClickListener : ((Map<String,Any>) -> Unit)?= null

    fun setOnItemClickListener(listener:(Map<String,Any>)->Unit){
        onItemClickListener = listener
    }

}