package com.example.customeprintservice.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customeprintservice.R;
import com.example.customeprintservice.print.MyItemRecyclerViewAdapter;
import com.example.customeprintservice.room.SelectedFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class PrintPreviewAdapter extends RecyclerView.Adapter<PrintPreviewAdapter.ViewHolder>{
    public Context context;
    private final List<File> mValues;

    public PrintPreviewAdapter(List<File> items) {
        mValues = items;
    }


    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.file_preview_image, parent, false);

        return new PrintPreviewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File file = mValues.get(position);
        if(file.exists()){

            Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            holder.image.setImageBitmap(myBitmap);

        }

    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final ImageView image;

        public ViewHolder(View view) {
            super(view);
            image = view.findViewById(R.id.fileImage);
        }

        @Override
        public String toString() {
            return super.toString();
        }

    }
}

