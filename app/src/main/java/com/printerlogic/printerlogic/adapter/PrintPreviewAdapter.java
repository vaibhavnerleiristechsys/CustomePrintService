package com.printerlogic.printerlogic.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.printerlogic.printerlogic.R;
import java.io.File;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_preview_image, parent, false);
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

