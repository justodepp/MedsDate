package com.medsdate.ui.main

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.medsdate.R
import com.medsdate.utils.Utility.loadImage
import java.util.*

class GalleryImageAdapter(private val mContext: Context, private var imgPath: ArrayList<String?>) : BaseAdapter() {
    override fun getCount(): Int {
        return imgPath.size //returns total of items in the list
    }

    override fun getItem(position: Int): Any {
        return imgPath[position]!! //returns list item at the specified position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View, parent: ViewGroup): View { // inflate the layout for each list row
        val thisConvertView = convertView
        // get current item to be displayed
        val currentItem = getItem(position) as String
        // get the TextView for item name and item description
        val imageView = thisConvertView.findViewById<View>(R.id.img_gallery) as ImageView
        //sets the image from path
        Glide.with(mContext)
                .load(loadImage(mContext, currentItem))
                .into(imageView)
        // returns the view for the current row
        return thisConvertView
    }
}