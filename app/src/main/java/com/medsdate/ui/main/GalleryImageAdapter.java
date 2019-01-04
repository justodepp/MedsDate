package com.medsdate.ui.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.medsdate.R;
import com.medsdate.utils.GlideApp;
import com.medsdate.utils.Utility;

import java.util.ArrayList;

public class GalleryImageAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<String> imgPath = new ArrayList();

    //public constructor
    public GalleryImageAdapter(Context context, ArrayList<String> imgPath) {
        this.mContext = context;
        this.imgPath = imgPath;
    }

    @Override
    public int getCount() {
        return imgPath.size(); //returns total of items in the list
    }

    @Override
    public Object getItem(int position) {
        return imgPath.get(position); //returns list item at the specified position
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // inflate the layout for each list row
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).
                    inflate(R.layout.item_gallery, parent, false);
        }

        // get current item to be displayed
        String currentItem = (String) getItem(position);

        // get the TextView for item name and item description
        ImageView imageView = (ImageView)convertView.findViewById(R.id.img_gallery);

        //sets the image from path
        GlideApp.with(mContext)
                .load(Utility.INSTANCE.loadImage(mContext, currentItem))
                .into(imageView);

        // returns the view for the current row
        return convertView;
    }
}