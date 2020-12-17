package com.example.customeprintservice.print;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.customeprintservice.R;
import com.example.customeprintservice.prefs.SignInCompanyPrefs;
import com.example.customeprintservice.print.dummy.DummyContent;
import com.example.customeprintservice.room.SelectedFile;
import com.example.customeprintservice.room.ServerSelectedFile;

import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 */
public class ServerPrintRelaseFragment extends Fragment {

    public static ArrayList serverDocumentlist = new ArrayList<SelectedFile>();

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ServerPrintRelaseFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static ServerPrintRelaseFragment newInstance(int columnCount) {
        ServerPrintRelaseFragment fragment = new ServerPrintRelaseFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_server_print_relase_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }

            PrintReleaseFragment printReleaseFragment =new PrintReleaseFragment();
            printReleaseFragment.getJobStatusesForServerList(context);

         //   recyclerView.setAdapter(new MyItemRecyclerViewAdapter(DummyContent.ITEMS));
            recyclerView.setAdapter(new MyItemRecyclerViewAdapter(serverDocumentlist));
        }
        return view;
    }
}