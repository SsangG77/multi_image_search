package com.sangjin.multi_image_search


import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class imageAdapter(private val uriList: ArrayList<Uri>, val context:Context) :
    RecyclerView.Adapter<imageAdapter.imageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): imageViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_item, parent, false)
        return imageViewHolder(view)
    }

    override fun onBindViewHolder(holder: imageViewHolder, position: Int) {
        val item = uriList[position]
        Glide.with(context)
            .load(item)
            .override(300, 300)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return uriList.size
    }

    class imageViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview) {
        var imageView = itemView.findViewById<ImageView>(R.id.image)
    }
}