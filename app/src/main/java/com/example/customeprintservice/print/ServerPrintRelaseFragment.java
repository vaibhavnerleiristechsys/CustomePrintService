package com.example.customeprintservice.print;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customeprintservice.MainActivity;
import com.example.customeprintservice.R;
import com.example.customeprintservice.adapter.FragmentPrinterListAdapter;
import com.example.customeprintservice.jipp.PrintActivity;
import com.example.customeprintservice.jipp.PrintUtils;
import com.example.customeprintservice.jipp.PrinterList;
import com.example.customeprintservice.room.SelectedFile;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.net.URI;


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
    public static String localPrinturl;
    public static int secure_release;


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


        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

        PrintReleaseFragment printReleaseFragment = new PrintReleaseFragment();
        printReleaseFragment.getJobStatusesForServerList(requireContext());


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


            Intent intent = new Intent("qrcodefloatingbutton");
            intent.putExtra("qrCodeScanBtn","Active");
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

            LocalBroadcastManager.getInstance(context).registerReceiver(mMessageReceiver,
                    new IntentFilter("message_subject_intent"));


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


    public BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String name= intent.getStringExtra("name");
            if(floatingActionButton!=null) {
                floatingActionButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.bloodOrange));
            }
        }
    };



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
            SelectedFile selectedFile=new SelectedFile();

            if(BottomNavigationActivityForServerPrint.selectedServerFile.size()>0) {
                 selectedFile = BottomNavigationActivityForServerPrint.selectedServerFile.get(0);
            }
           if(selectedFile.isFromApi()==true){
               if(secure_release == 0 || secure_release ==1 || secure_release==2){
                   Toast.makeText(requireContext(), "print release", Toast.LENGTH_LONG)
                           .show();
                   printReleaseFragment.releaseJob(context);
                   dialog.cancel();
                   Intent myIntent = new Intent(getActivity(), MainActivity.class);
                   getActivity().startActivity(myIntent);

               }else if(secure_release == 3 || secure_release ==4){
                   Toast.makeText(requireContext(), "print hold", Toast.LENGTH_LONG)
                           .show();
                   dialog.cancel();
                   Intent myIntent = new Intent(getActivity(), MainActivity.class);
                   getActivity().startActivity(myIntent);

               }
               else if(secure_release == 5 || secure_release ==6){
                   Toast.makeText(requireContext(), "prompt dialog open", Toast.LENGTH_LONG)
                           .show();
                   dialogPromptPrinter();
               }

            }else{

               if(secure_release == 0 || secure_release ==1 || secure_release==2){
                     String FilePath =selectedFile.getFilePath();
                    PrintActivity printActivity =new PrintActivity();
                    printActivity.locaPrint(FilePath,localPrinturl,context);

                   Toast.makeText(requireContext(), "print release", Toast.LENGTH_LONG)
                           .show();
                       dialog.cancel();
                     Intent myIntent = new Intent(getActivity(), MainActivity.class);
                      getActivity().startActivity(myIntent);

               }else if(secure_release == 3 || secure_release ==4){
                   Toast.makeText(requireContext(), "print hold", Toast.LENGTH_LONG)
                           .show();
                   dialog.cancel();
                   Intent myIntent = new Intent(getActivity(), MainActivity.class);
                   getActivity().startActivity(myIntent);
               }
               else if(secure_release == 5 || secure_release ==6){
                   Toast.makeText(requireContext(), "prompt dialog open", Toast.LENGTH_LONG)
                           .show();
                   dialogPromptPrinter();
               }

            }

        });
        dialog.show();
    }



    private void dialogPromptPrinter(){
        Dialog dialog1 = new Dialog(requireContext());
        //  dialog.setContentView(R.layout.dialog_select_printer);
        v = LayoutInflater.from(context).inflate(R.layout.dialog_printer_prompt, null);
        dialog1.setContentView(v);
        dialog1.setCancelable(false);

        Button hold = dialog1.findViewById(R.id.hold);
        Button release= dialog1.findViewById(R.id.release);
        dialog1.setCanceledOnTouchOutside(true);
        Window window = dialog1.getWindow();
        assert window != null;
        window.setLayout(AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT);
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setDimAmount(0.5f);
        window.setAttributes(wlp);
        dialog1.show();

        hold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
                dialog1.cancel();
            }
        });

        release.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectedFile selectedFile=new SelectedFile();

                if(BottomNavigationActivityForServerPrint.selectedServerFile.size()>0) {
                    selectedFile = BottomNavigationActivityForServerPrint.selectedServerFile.get(0);
                }
                if(selectedFile.isFromApi()==true) {

                }else{
                      String FilePath =selectedFile.getFilePath();
                     PrintActivity printActivity =new PrintActivity();
                     printActivity.locaPrint(FilePath,localPrinturl,context);
                }

                }

        });

    }
}