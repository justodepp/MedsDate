package com.medsdate.ui.main;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.medsdate.R;
import com.medsdate.utils.GlideApp;

import java.io.IOException;
import java.io.InputStream;
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
                .load("")
                .error(loadImage(currentItem))
                .into(imageView);

        // returns the view for the current row
        return convertView;
    }

    // create a new ImageView for each item referenced by the Adapter
    /*public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }

        // The images are in /app/assets/images/thumbnails/example.jpeg
        imageView.setImageDrawable(loadThumb("images/thumbnails/" + data.get(position) + ".jpeg"));
        return imageView;
    }*/

    // references to our images
    private Drawable loadImage(String name) {
        try {
            // get input stream
            InputStream ims = mContext.getAssets().open("images/"+name);
            // load image as Drawable
            Drawable d = Drawable.createFromStream(ims, null);
            // set image to ImageView
            return d;
        } catch (IOException ex) {
            return null;
        }
    }
}