package com.example.customeprintservice.print;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
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
import com.example.customeprintservice.prefs.LoginPrefs;
import com.example.customeprintservice.prefs.SignInCompanyPrefs;
import com.example.customeprintservice.printjobstatus.model.getjobstatuses.GetJobStatusesResponse;
import com.example.customeprintservice.printjobstatus.model.getjobstatuses.PrintQueueJobStatusItem;
import com.example.customeprintservice.rest.ApiService;
import com.example.customeprintservice.rest.RetrofitClient;
import com.example.customeprintservice.room.SelectedFile;
import com.example.customeprintservice.utils.ProgressDialog;
import com.google.android.gms.common.util.ArrayUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.net.URI;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


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
    public  ArrayList<SelectedFile> localdocumentFromsharedPrefences =new ArrayList<SelectedFile>();
    RecyclerView recyclerView;

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
        ProgressDialog.Companion.showLoadingDialog(requireContext(), "please wait");
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
        getjobListStatus();
            }
        }, 5000);
    //    PrintReleaseFragment printReleaseFragment = new PrintReleaseFragment();
     //   printReleaseFragment.getJobStatusesForServerList(requireContext());


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_server_print_relase_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
             context = view.getContext();
             recyclerView = (RecyclerView) view;
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



            //    recyclerView.setAdapter(new MyItemRecyclerViewAdapter(PrintReleaseFragment.Companion.getGetdocumentList()));


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
                Intent myIntent = new Intent(getActivity(), MainActivity.class);
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

                   dialogPromptPrinter();
               }

            }else{

               if(secure_release == 0 || secure_release ==1 || secure_release==2){
                     String FilePath =selectedFile.getFilePath();
                    PrintActivity printActivity =new PrintActivity();
                    printActivity.locaPrint(FilePath,localPrinturl,context);
                   removeDocumentFromSharedPreferences();
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
                Intent myIntent = new Intent(getActivity(), MainActivity.class);
                getActivity().startActivity(myIntent);
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
                    PrintReleaseFragment printReleaseFragment=new PrintReleaseFragment();
                    printReleaseFragment.releaseJob(context);
                    dialog.cancel();
                    dialog1.cancel();
                    Intent myIntent = new Intent(getActivity(), MainActivity.class);
                    getActivity().startActivity(myIntent);
                }else{
                      String FilePath =selectedFile.getFilePath();
                     PrintActivity printActivity =new PrintActivity();
                     printActivity.locaPrint(FilePath,localPrinturl,context);
                    removeDocumentFromSharedPreferences();
                    dialog.cancel();
                    dialog1.cancel();
                    Intent myIntent = new Intent(getActivity(), MainActivity.class);
                    getActivity().startActivity(myIntent);
                }

                }

        });

    }

   public void removeDocumentFromSharedPreferences(){
       SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
       Gson gson = new Gson();
       String json = prefs.getString("localdocumentlist", null);
       Type type = new TypeToken<ArrayList<SelectedFile>>() {}.getType();
       localdocumentFromsharedPrefences = gson.fromJson(json, type);
       SelectedFile selectedFile=new SelectedFile();

       if(BottomNavigationActivityForServerPrint.selectedServerFile.size()>0) {
           selectedFile = BottomNavigationActivityForServerPrint.selectedServerFile.get(0);
       }

       for(int i=0;i<localdocumentFromsharedPrefences.size();i++){
           SelectedFile selectedFile1 =  localdocumentFromsharedPrefences.get(i);
           if(selectedFile1.getFilePath().equals(selectedFile.getFilePath())){
                   localdocumentFromsharedPrefences.remove(i);
           }
       }

       SharedPreferences prefs1 = PreferenceManager.getDefaultSharedPreferences(context);
       Gson gson1 = new Gson();
       SharedPreferences.Editor editor = prefs1.edit();
       String json1 = gson1.toJson(localdocumentFromsharedPrefences);
       editor.putString("localdocumentlist", json1);
       editor.apply();

    }


    public void getjobListStatus(){
        ProgressDialog.Companion.showLoadingDialog(requireContext(), "Loading");
        PrintReleaseFragment printReleaseFragment=new PrintReleaseFragment();
        PrintReleaseFragment.Companion.getGetdocumentList().clear();
        String BASE_URL = "https://gw.app.printercloud.com/devncookta/pq/api/job-statuses/";
        ApiService apiService = new RetrofitClient(requireContext())
                .getRetrofitInstance(BASE_URL)
                .create(ApiService.class);


        Call call = apiService.getPrintJobStatuses(
                "Bearer " + LoginPrefs.Companion.getOCTAToken(requireContext()),
                printReleaseFragment.decodeJWT(requireContext()),
                SignInCompanyPrefs.Companion.getIdpType(requireContext()).toString(),
                SignInCompanyPrefs.Companion.getIdpName(requireContext()).toString()
        );
        call.enqueue(new Callback<GetJobStatusesResponse>() {
            public void onResponse(Call<GetJobStatusesResponse> call, Response<GetJobStatusesResponse> response) {
                if(response.isSuccessful()){
                List<PrintQueueJobStatusItem> getJobStatusesResponse = response.body().getPrintQueueJobStatus();
                    PrintReleaseFragment.Companion.getGetdocumentList().clear();
                for (int i = 0; i < getJobStatusesResponse.size(); i++) {
                    PrintQueueJobStatusItem PrintQueueJobStatusItem = getJobStatusesResponse.get(i);
                    SelectedFile selectedFile = new SelectedFile();
                    selectedFile.setFromApi(true);
                    selectedFile.setFileName(PrintQueueJobStatusItem.getDocumentTitle());
                    selectedFile.setFileSelectedDate(PrintQueueJobStatusItem.getSubmittedAtRelative());
                    selectedFile.setFilePath(PrintQueueJobStatusItem.getDocumentTitle());
                    selectedFile.setJobNum(PrintQueueJobStatusItem.getJobNumber());
                    selectedFile.setJobType(1);
                    selectedFile.setQueueId(PrintQueueJobStatusItem.getPrinterDeviceQueueId());
                    selectedFile.setUserName(PrintQueueJobStatusItem.getUserName());
                    selectedFile.setWorkStationId(PrintQueueJobStatusItem.getWorkstationId());
                    PrintReleaseFragment.Companion.getGetdocumentList().add(selectedFile);
                }
                localdocumentFromsharedPrefences.clear();
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                Gson gson = new Gson();
                String json = prefs.getString("localdocumentlist", null);
                Type type = new TypeToken<ArrayList<SelectedFile>>() {
                }.getType();
                localdocumentFromsharedPrefences = gson.fromJson(json, type);

                if(localdocumentFromsharedPrefences!=null) {
                    PrintReleaseFragment.Companion.getGetdocumentList().addAll(localdocumentFromsharedPrefences);
                }
                recyclerView.setAdapter(new MyItemRecyclerViewAdapter(PrintReleaseFragment.Companion.getGetdocumentList()));

                    ProgressDialog.Companion.cancelLoading();
            }
        }

            @Override
            public void onFailure(Call<GetJobStatusesResponse> call, Throwable t) {
                ProgressDialog.Companion.cancelLoading();
                call.cancel();
            }
        });




    }
}