package com.example.customeprintservice.print;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.customeprintservice.R;
import com.example.customeprintservice.room.SelectedFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * TODO: Replace the implementation with code for your data type.
 */
public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {

    public static Set<SelectedFile> selectedServerFile = new HashSet<SelectedFile>();
    private final List<SelectedFile> mValues;
    Menu menu;
    List<ViewHolder> holders= new ArrayList<ViewHolder>();
    public MyItemRecyclerViewAdapter(List<SelectedFile> items) {
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_server_print_relase, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).getFileName());
        holder.mContentView.setText(mValues.get(position).getFileSelectedDate());

        holders.add(holder);

        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d("file name", Objects.requireNonNull(mValues.get(position).getFileName()));
                BottomNavigationActivityForServerPrint.selectedServerFile.add(mValues.get(position));


            }

        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public final LinearLayout serverDocument;
        public final CheckBox checkBox;
        public SelectedFile mItem;
        public ImageView documenticon;
        List a =new ArrayList<View>();
        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = view.findViewById(R.id.item_number);
            mContentView = view.findViewById(R.id.content);
            serverDocument = view.findViewById(R.id.serverDocument);
            checkBox = view.findViewById(R.id.checkbox);
            checkBox.setVisibility(View.GONE);
            documenticon =view.findViewById(R.id.documenticon);


            documenticon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Log.d( "document click", ":successfull");
                    setDocument();
                }

            });

            serverDocument.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Log.d( "document click", ":successfull");
                    setDocument();
                }

            });


        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }

        public void setDocument(){
            for(int i=0;i<holders.size();i++){
                ViewHolder holder1 = holders.get(i);
                holder1.checkBox.setVisibility(View.VISIBLE);
                holder1.documenticon.setVisibility(View.GONE);
            }
        }
    }
}