package com.example.customeprintservice.print;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customeprintservice.R;
import com.example.customeprintservice.adapter.FragmentPrinterListAdapter;
import com.example.customeprintservice.jipp.PrinterList;
import com.example.customeprintservice.room.SelectedFile;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 */
public class ServerPrintRelaseFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    public static ArrayList serverDocumentlist = new ArrayList<SelectedFile>();
    // TODO: Customize parameters
    private int mColumnCount = 1;
    Context context;
    Dialog dialog;
    View v = null;
    FloatingActionButton floatingActionButton;
    public static String checkMenu="search";


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
        setHasOptionsMenu(true);
        //PrintReleaseFragment printReleaseFragment = new PrintReleaseFragment();
        //printReleaseFragment.getJobStatusesForServerList(getContext());

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
             context = view.getContext();

            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }



              //   recyclerView.setAdapter(new MyItemRecyclerViewAdapter(DummyContent.ITEMS));
              recyclerView.setAdapter(new MyItemRecyclerViewAdapter(serverDocumentlist));

        }
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
            inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.download:
                PrintReleaseFragment printReleaseFragment =new PrintReleaseFragment();
                printReleaseFragment.cancelJob(context);
                Intent myIntent = new Intent(getActivity(), BottomNavigationActivity.class);
                getActivity().startActivity(myIntent);
                return (true);
            case R.id.print:
                selectePrinterDialog();
                return (true);
        }
        return (super.onOptionsItemSelected(item));
    }

    private void selectePrinterDialog() {
        dialog = new Dialog(requireContext());
      //  dialog.setContentView(R.layout.dialog_select_printer);
        v = LayoutInflater.from(context).inflate(R.layout.dialog_select_printer, null);
        dialog.setContentView(v);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(true);
        Window window = dialog.getWindow();
        assert window != null;
        window.setLayout(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.MATCH_PARENT);
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setDimAmount(0.5f);
        window.setAttributes(wlp);

        RecyclerView printerRecyclerView = dialog.findViewById(R.id.dialogSelectPrinterRecyclerView);
        ImageView imgCancel = dialog.findViewById(R.id.imgDialogSelectPrinterCancel);
        floatingActionButton = dialog.findViewById(R.id.dialogSelectPrinterFloatingButton);
        floatingActionButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.paleGray));

        printerRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        PrinterList printerList = new PrinterList();
        printerRecyclerView.setAdapter(new FragmentPrinterListAdapter(requireContext(), printerList.getPrinterList()));


        imgCancel.setOnClickListener(v ->
                dialog.cancel()
        );

        floatingActionButton.setOnClickListener(v -> {
            PrintReleaseFragment printReleaseFragment=new PrintReleaseFragment();
            printReleaseFragment.releaseJob(context);
            dialog.cancel();
            Intent myIntent = new Intent(getActivity(), BottomNavigationActivity.class);
            getActivity().startActivity(myIntent);
        });
        dialog.show();
    }



}