package com.example.customeprintservice.print;

import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.customeprintservice.R;
import com.example.customeprintservice.print.dummy.DummyContent.DummyItem;
import com.example.customeprintservice.room.SelectedFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {

    private final List<SelectedFile> mValues;
    public static Set<SelectedFile> selectedServerFile =new HashSet<SelectedFile>();
    Menu menu;

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

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.item_number);
            mContentView = (TextView) view.findViewById(R.id.content);
            serverDocument = (LinearLayout)view.findViewById(R.id.serverDocument);
            checkBox = (CheckBox)view.findViewById(R.id.checkbox);

        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}