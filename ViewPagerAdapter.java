package com.example.marvelselect;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;

public class ViewPagerAdapter extends RecyclerView.Adapter<ViewPagerAdapter.ViewPagerViewHolder> {

    //ArrayList of Bitmap images accessed from JSON
    ArrayList<Bitmap> images;

    //constructor
    public ViewPagerAdapter(ArrayList<Bitmap> images) {
        //set variables
        this.images = images;
    }//ViewPagerAdapter

    @NonNull
    @Override
    public ViewPagerAdapter.ViewPagerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_pager_head_item, parent, false);
        return new ViewPagerViewHolder(view);
    }//onCreateViewHolder

    @Override
    public void onBindViewHolder(@NonNull ViewPagerAdapter.ViewPagerViewHolder holder, int position) {
        holder.image.setImageBitmap(images.get(position));
    }//onBindViewHolder

    @Override
    public int getItemCount() {
        return images.size();
    }//getItemCount

    public class ViewPagerViewHolder extends RecyclerView.ViewHolder {
        //UI variables
        ImageView image;

        public ViewPagerViewHolder(@NonNull View itemView) {
            super(itemView);
            //find views
            image = itemView.findViewById(R.id.id_viewPager_imageView);
        }
    }//ViewPagerViewHolder
}
