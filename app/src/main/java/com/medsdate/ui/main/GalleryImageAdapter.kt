package com.medsdate.ui.main

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.medsdate.R
import com.medsdate.utils.Utility.loadImage

class GalleryImageAdapter(private val mContext: Context, private var imgPath: Array<String?>?) : BaseAdapter() {
    override fun getCount(): Int {
        return imgPath?.size ?: 0//returns total of items in the list
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View { // inflate the layout for each list row

        val thisConvertView = LayoutInflater.from(mContext).inflate(R.layout.item_gallery, parent, false)
        // get current item to be displayed
        val currentItem = getItem(position) as String
        // get the TextView for item name and item description
        val imageView = thisConvertView.findViewById<ImageView>(R.id.img_gallery)
        //sets the image from path
        Glide.with(mContext)
                .load(loadImage(mContext, currentItem))
                .into(imageView)
        // returns the view for the current row
        return thisConvertView
    }

    override fun getItem(position: Int): Any {
        return imgPath?.get(position)!! //returns list item at the specified position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}